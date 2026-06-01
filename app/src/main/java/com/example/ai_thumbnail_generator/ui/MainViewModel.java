package com.example.ai_thumbnail_generator.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ai_thumbnail_generator.network.Models;
import com.example.ai_thumbnail_generator.network.ServerService;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainViewModel extends AndroidViewModel {

    private static final String SERVER_URL = "https://germinable-chunkily-breanne.ngrok-free.dev";

    private final ServerService serverService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Models.ThumbnailData>> history = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>("");
    private final MutableLiveData<String> lastGeneratedUri = new MutableLiveData<>(null);

    public enum GenStatus { IDLE, PROCESSING, COMPLETED, FAILED }
    private final MutableLiveData<GenStatus> generationStatus = new MutableLiveData<>(GenStatus.IDLE);
    public LiveData<GenStatus> getGenerationStatus() { return generationStatus; }

    private Socket mSocket;

    public MainViewModel(@NonNull Application application) {
        super(application);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serverService = retrofit.create(ServerService.class);

        setupSocket();
        fetchHistory();
    }

    private void setupSocket() {
        try {
            mSocket = IO.socket(SERVER_URL);
            mSocket.connect();

            mSocket.on("status_update", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String message = data.getString("message");
                    mainHandler.post(() -> {
                        statusMessage.setValue(message);
                        generationStatus.setValue(GenStatus.PROCESSING);
                    });
                } catch (Exception e) {
                    Log.e("Socket", "Status update error", e);
                }
            });

            mSocket.on("thumbnail_ready", args -> {
                try {
                    org.json.JSONObject data = (org.json.JSONObject) args[0];
                    String imageUrl = data.getString("imageUrl");
                    org.json.JSONObject strategy = data.getJSONObject("strategy");
                    String hookText = strategy.getString("hookText");
                    String colorPalette = strategy.getString("colorPalette");
                    String ratio = data.getString("ratio");

                    executor.execute(() -> {
                        try {
                            android.graphics.Bitmap bg = com.bumptech.glide.Glide.with(getApplication())
                                    .asBitmap()
                                     .load(imageUrl)
                                    .submit()
                                    .get();

                            android.graphics.Bitmap finalBitmap = com.example.ai_thumbnail_generator.utils.ThumbnailRenderer.draw(
                                    getApplication(), bg, hookText, colorPalette, ratio);

                            mainHandler.post(() -> {
                                lastGeneratedUri.setValue(imageUrl); // Keep original URL for reference
                                statusMessage.setValue("Generation complete with text!");
                                generationStatus.setValue(GenStatus.COMPLETED);
                                fetchHistory();
                                
                                // Auto-hide after 3 seconds
                                mainHandler.postDelayed(() -> {
                                    if (generationStatus.getValue() == GenStatus.COMPLETED) {
                                        generationStatus.setValue(GenStatus.IDLE);
                                    }
                                }, 3000);
                            });
                        } catch (Exception e) {
                            Log.e("Render", "Failed to render text", e);
                            mainHandler.post(() -> {
                                lastGeneratedUri.setValue(imageUrl);
                                generationStatus.setValue(GenStatus.COMPLETED); // Still completed even if text fails
                                fetchHistory();
                                
                                mainHandler.postDelayed(() -> {
                                    if (generationStatus.getValue() == GenStatus.COMPLETED) {
                                        generationStatus.setValue(GenStatus.IDLE);
                                    }
                                }, 3000);
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e("Socket", "Error parsing result", e);
                    mainHandler.post(() -> {
                        generationStatus.setValue(GenStatus.FAILED);
                        mainHandler.postDelayed(() -> {
                            if (generationStatus.getValue() == GenStatus.FAILED) {
                                generationStatus.setValue(GenStatus.IDLE);
                            }
                        }, 3000);
                    });
                }
            });

        } catch (Exception e) {
            Log.e("Socket", "Socket initialization error", e);
        }
    }

    public void generateThumbnail(String title, String ratio, String type) {
        statusMessage.setValue("Processing request...");
        generationStatus.setValue(GenStatus.PROCESSING);
        
        String socketId = mSocket.id();
        
        com.example.ai_thumbnail_generator.utils.SessionManager sessionManager = new com.example.ai_thumbnail_generator.utils.SessionManager(getApplication());
        String userId = sessionManager.getUserId();

        Models.GenerateRequest request = new Models.GenerateRequest(title, ratio, socketId, userId, type);
        
        executor.execute(() -> {
            try {
                serverService.startGeneration(request).execute();
            } catch (Exception e) {
                mainHandler.post(() -> {
                    statusMessage.setValue("Error: " + e.getMessage());
                    generationStatus.setValue(GenStatus.FAILED);
                    
                    // Auto-hide error toast after 3 seconds
                    mainHandler.postDelayed(() -> {
                        if (generationStatus.getValue() == GenStatus.FAILED) {
                            generationStatus.setValue(GenStatus.IDLE);
                        }
                    }, 3000);
                });
            }
        });
    }

    public void fetchHistory() {
        executor.execute(() -> {
            try {
                retrofit2.Response<List<Models.ThumbnailData>> response = serverService.getHistory().execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> history.setValue(response.body()));
                }
            } catch (Exception e) {
                Log.e("API", "History fetch error", e);
            }
        });
    }

    public void togglePublic(String id) {
        executor.execute(() -> {
            try {
                retrofit2.Response<okhttp3.ResponseBody> response = serverService.togglePublic(id).execute();
                if (response.isSuccessful()) {
                    fetchHistory();
                }
            } catch (Exception e) {
                Log.e("API", "Toggle public error", e);
            }
        });
    }

    public void deleteThumbnail(String id) {
        executor.execute(() -> {
            try {
                retrofit2.Response<okhttp3.ResponseBody> response = serverService.deleteThumbnail(id).execute();
                if (response.isSuccessful()) {
                    fetchHistory();
                }
            } catch (Exception e) {
                Log.e("API", "Delete thumbnail error", e);
            }
        });
    }

    public void likeThumbnail(String id) {
        com.example.ai_thumbnail_generator.utils.SessionManager sessionManager = new com.example.ai_thumbnail_generator.utils.SessionManager(getApplication());
        String userId = sessionManager.getUserId();
        executor.execute(() -> {
            try {
                retrofit2.Response<okhttp3.ResponseBody> response = serverService.likeThumbnail(id, userId).execute();
                if (response.isSuccessful()) {
                    fetchHistory();
                }
            } catch (Exception e) {
                Log.e("API", "Like thumbnail error", e);
            }
        });
    }

    public void unlikeThumbnail(String id) {
        com.example.ai_thumbnail_generator.utils.SessionManager sessionManager = new com.example.ai_thumbnail_generator.utils.SessionManager(getApplication());
        String userId = sessionManager.getUserId();
        executor.execute(() -> {
            try {
                retrofit2.Response<okhttp3.ResponseBody> response = serverService.unlikeThumbnail(id, userId).execute();
                if (response.isSuccessful()) {
                    fetchHistory();
                }
            } catch (Exception e) {
                Log.e("API", "Unlike thumbnail error", e);
            }
        });
    }

    public LiveData<List<Models.ThumbnailData>> getHistory() { return history; }
    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<String> getLastGeneratedUri() { return lastGeneratedUri; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}
