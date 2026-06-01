package com.example.ai_thumbnail_generator.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Models {

    // --- OpenRouter Models ---
    public static class OpenRouterRequest {
        public String model = "meta-llama/llama-3-8b-instruct:free";
        public List<Message> messages;
        public ResponseFormat response_format = new ResponseFormat();

        public OpenRouterRequest(List<Message> messages) {
            this.messages = messages;
        }
    }

    public static class GenerateRequest {
        public String title;
        public String ratio;
        public String socketId;
        public String userId;
        public String type;

        public GenerateRequest(String title, String ratio, String socketId, String userId, String type) {
            this.title = title;
            this.ratio = ratio;
            this.socketId = socketId;
            this.userId = userId;
            this.type = type;
        }
    }

    public static class GenerateResponse {
        public boolean success;
        public String generationId;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ResponseFormat {
        public String type = "json_object";
    }

    public static class OpenRouterResponse {
        public List<Choice> choices;

        public static class Choice {
            public Message message;
        }
    }

    public static class ThumbnailStrategy {
        public String styleType;
        public String hookText;
        public String colorPalette;
        public String visualPrompt;
    }

    public static class ThumbnailData {
        public String id;
        public String user_id;
        public String original_title;
        public String hook_text;
        public String color_palette;
        public String image_url;
        public String ratio_type;
        public String created_at;
        public boolean is_public;
        public int likes;
        public Profile profiles;

        public static class Profile {
            public String display_name;
            public String avatar_url;
        }
    }

    // --- Leonardo Models ---
    public static class LeonardoGenerationRequest {
        public String prompt;
        public String modelId = "6bef9f1b-29cb-40c7-b9df-cd99d9ff2034"; // Default model
        public int width;
        public int height;
        public int num_images = 1;

        public LeonardoGenerationRequest(String prompt, int width, int height) {
            this.prompt = prompt;
            this.width = width;
            this.height = height;
        }
    }

    public static class LeonardoGenerationResponse {
        public Generation sdGenerationJob;

        public static class Generation {
            public String generationId;
        }
    }

    public static class LeonardoGetGenerationResponse {
        public Generations generations_by_pk;

        public static class Generations {
            public String status;
            public List<GeneratedImage> generated_images;
        }

        public static class GeneratedImage {
            public String url;
        }
    }
}
