package com.example.ai_thumbnail_generator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LeonardoService {
    @POST("generations")
    Call<Models.LeonardoGenerationResponse> createGeneration(
            @Header("Authorization") String token,
            @Body Models.LeonardoGenerationRequest request
    );

    @GET("generations/{id}")
    Call<Models.LeonardoGetGenerationResponse> getGeneration(
            @Header("Authorization") String token,
            @Path("id") String generationId
    );
}
