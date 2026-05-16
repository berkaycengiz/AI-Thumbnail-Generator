package com.example.ai_thumbnail_generator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OpenRouterService {
    @POST("chat/completions")
    Call<Models.OpenRouterResponse> getThumbnailStrategy(
            @Header("Authorization") String token,
            @Body Models.OpenRouterRequest request
    );
}
