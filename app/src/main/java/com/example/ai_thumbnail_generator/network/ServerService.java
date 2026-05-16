package com.example.ai_thumbnail_generator.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerService {

    @POST("api/generate")
    Call<Models.GenerateResponse> startGeneration(@Body Models.GenerateRequest request);

    @GET("api/history")
    Call<List<Models.ThumbnailData>> getHistory();
}