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
