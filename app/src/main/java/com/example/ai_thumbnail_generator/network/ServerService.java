package com.example.ai_thumbnail_generator.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerService {
    
    class GenerateRequest {
        public String title;
        public String ratio;
        public String socketId;

        public GenerateRequest(String title, String ratio, String socketId) {
            this.title = title;
            this.ratio = ratio;
            this.socketId = socketId;
        }
    }

    class GenerateResponse {
        public boolean success;
        public String generationId;
    }

    class ThumbnailResponse {
        public int id;
        public String title;
        public String hook_text;
        public String color_palette;
        public String image_url;
        public String ratio_type;
        public String created_at;
    }

    @POST("api/generate")
    Call<GenerateResponse> startGeneration(@Body GenerateRequest request);

    @GET("api/history")
    Call<List<ThumbnailResponse>> getHistory();
}
