package com.example.ai_thumbnail_generator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SupabaseAuthService {

    @POST("auth/v1/token?grant_type=id_token")
    Call<AuthResponse> signInWithIdToken(
            @Header("apikey") String apikey,
            @Body IdTokenRequest request
    );

    class IdTokenRequest {
        public String id_token;
        public String provider;

        public IdTokenRequest(String id_token) {
            this.id_token = id_token;
            this.provider = "google";
        }
    }

    class AuthResponse {
        public String access_token;
        public String refresh_token;
        public User user;
    }

    class User {
        public String id;
    }
}
