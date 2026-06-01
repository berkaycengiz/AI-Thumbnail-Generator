package com.example.ai_thumbnail_generator.network;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServerService {

    @POST("api/generate")
    Call<Models.GenerateResponse> startGeneration(@Body Models.GenerateRequest request);

    @GET("api/history")
    Call<List<Models.ThumbnailData>> getHistory();

    @POST("api/thumbnails/{id}/toggle-public")
    Call<ResponseBody> togglePublic(@Path("id") String id);

    @DELETE("api/thumbnails/{id}")
    Call<ResponseBody> deleteThumbnail(@Path("id") String id);

    @POST("api/thumbnails/{id}/like")
    Call<ResponseBody> likeThumbnail(@Path("id") String id, @Query("userId") String userId);

    @POST("api/thumbnails/{id}/unlike")
    Call<ResponseBody> unlikeThumbnail(@Path("id") String id, @Query("userId") String userId);
}