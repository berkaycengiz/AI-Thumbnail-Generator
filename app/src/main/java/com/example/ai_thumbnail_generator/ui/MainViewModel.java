package com.example.ai_thumbnail_generator.ui;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.example.ai_thumbnail_generator.db.AppDatabase;
import com.example.ai_thumbnail_generator.db.ThumbnailEntity;
import com.example.ai_thumbnail_generator.network.LeonardoService;
import com.example.ai_thumbnail_generator.network.Models;
import com.example.ai_thumbnail_generator.network.OpenRouterService;
import com.example.ai_thumbnail_generator.utils.ThumbnailRenderer;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends AndroidViewModel {

    // --- API KEYS (REPLACE WITH YOUR OWN) ---
    private static final String OPENROUTER_KEY = "Bearer sk-or-v1-YOUR_KEY_HERE";
    private static final String LEONARDO_KEY = "Bearer YOUR_KEY_HERE";

    private final OpenRouterService openRouterService;
    private final LeonardoService leonardoService;
    private final AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>("");
    private final MutableLiveData<String> finalImageUrl = new MutableLiveData<>("");
    private final MutableLiveData<ThumbnailEntity> lastGenerated = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);

        Retrofit orRetrofit = new Retrofit.Builder()
                .baseUrl("https://openrouter.ai/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openRouterService = orRetrofit.create(OpenRouterService.class);

        Retrofit leoRetrofit = new Retrofit.Builder()
                .baseUrl("https://cloud.leonardo.ai/api/rest/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        leonardoService = leoRetrofit.create(LeonardoService.class);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<ThumbnailEntity> getLastGenerated() { return lastGenerated; }
    public LiveData<List<ThumbnailEntity>> getHistory() { return database.thumbnailDao().getAllThumbnails(); }

    public void generateThumbnail(String title, String ratio) {
        isLoading.setValue(true);
        statusMessage.setValue("Analyzing video title...");

        // Step 1: OpenRouter Strategy
        String prompt = "Act as a social media expert. Create a thumbnail strategy for a video titled: '" + title + "'. " +
                "Return a JSON with: styleType, hookText (short & punchy), colorPalette (hex), visualPrompt (English description for AI image gen).";

        Models.OpenRouterRequest request = new Models.OpenRouterRequest(
                Collections.singletonList(new Models.Message("user", prompt))
        );

        openRouterService.getThumbnailStrategy(OPENROUTER_KEY, request).enqueue(new Callback<Models.OpenRouterResponse>() {
            @Override
            public void onResponse(Call<Models.OpenRouterResponse> call, Response<Models.OpenRouterResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String jsonContent = response.body().choices.get(0).message.content;
                    try {
                        Models.ThumbnailStrategy strategy = new Gson().fromJson(jsonContent, Models.ThumbnailStrategy.class);
                        startLeonardoGeneration(title, ratio, strategy);
                    } catch (Exception e) {
                        onError("Failed to parse AI strategy");
                    }
                } else {
                    onError("OpenRouter API error");
                }
            }

            @Override
            public void onFailure(Call<Models.OpenRouterResponse> call, Throwable t) {
                onError("Network failure: " + t.getMessage());
            }
        });
    }

    private void startLeonardoGeneration(String title, String ratio, Models.ThumbnailStrategy strategy) {
        statusMessage.postValue("Generating background image...");
        
        int w = 1280, h = 720;
        if (ratio.equals("1:1")) { w = 1024; h = 1024; }
        else if (ratio.equals("9:16")) { w = 768; h = 1344; } // Leonardo supported dims

        Models.LeonardoGenerationRequest req = new Models.LeonardoGenerationRequest(strategy.visualPrompt, w, h);

        leonardoService.createGeneration(LEONARDO_KEY, req).enqueue(new Callback<Models.LeonardoGenerationResponse>() {
            @Override
            public void onResponse(Call<Models.LeonardoGenerationResponse> call, Response<Models.LeonardoGenerationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pollLeonardo(title, ratio, strategy, response.body().sdGenerationJob.generationId);
                } else {
                    onError("Leonardo Generation failed");
                }
            }

            @Override
            public void onFailure(Call<Models.LeonardoGenerationResponse> call, Throwable t) {
                onError("Leonardo network error");
            }
        });
    }

    private void pollLeonardo(String title, String ratio, Models.ThumbnailStrategy strategy, String genId) {
        statusMessage.postValue("AI is painting (polling)...");
        
        executor.execute(() -> {
            boolean complete = false;
            String imageUrl = null;
            
            while (!complete) {
                try {
                    Thread.sleep(2500);
                    Response<Models.LeonardoGetGenerationResponse> response = leonardoService.getGeneration(LEONARDO_KEY, genId).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        Models.LeonardoGetGenerationResponse.Generations gen = response.body().generations_by_pk;
                        if ("COMPLETE".equals(gen.status)) {
                            complete = true;
                            if (!gen.generated_images.isEmpty()) {
                                imageUrl = gen.generated_images.get(0).url;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Polling", "Error", e);
                }
            }

            if (imageUrl != null) {
                downloadAndRender(title, ratio, strategy, imageUrl);
            } else {
                mainHandler.post(() -> onError("Image generation timed out"));
            }
        });
    }

    private void downloadAndRender(String title, String ratio, Models.ThumbnailStrategy strategy, String imageUrl) {
        statusMessage.postValue("Finalizing design...");
        
        executor.execute(() -> {
            try {
                Bitmap bg = Glide.with(getApplication())
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                Bitmap finalBitmap = ThumbnailRenderer.draw(getApplication(), bg, strategy.hookText, strategy.colorPalette, ratio);
                
                String savedUri = saveBitmapToLocal(finalBitmap);
                
                ThumbnailEntity entity = new ThumbnailEntity(
                        title, strategy.hookText, strategy.colorPalette, savedUri, ratio, System.currentTimeMillis()
                );
                
                database.thumbnailDao().insert(entity);
                
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    lastGenerated.setValue(entity);
                });

            } catch (Exception e) {
                mainHandler.post(() -> onError("Rendering failed"));
            }
        });
    }

    private String saveBitmapToLocal(Bitmap bitmap) {
        File folder = new File(getApplication().getFilesDir(), "thumbnails");
        if (!folder.exists()) folder.mkdirs();
        
        String fileName = "thumb_" + System.currentTimeMillis() + ".png";
        File file = new File(folder, fileName);
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    private void onError(String message) {
        isLoading.setValue(false);
        statusMessage.setValue("Error: " + message);
    }
}
