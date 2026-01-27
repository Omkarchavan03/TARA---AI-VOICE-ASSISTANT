package com.tara.core;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OllamaBrain {

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    private static final String MODEL_NAME = "mistral";

    private static final Gson gson = new Gson();

    /* =====================================================
       SYSTEM / COMMAND DETECTION (STRICT)
       ===================================================== */
    private static boolean isSystemCommand(String text) {

        if (text == null) return false;

        text = text.toLowerCase().trim();

        boolean verb =
                text.startsWith("open ")
             || text.startsWith("close ")
             || text.startsWith("webopen ");

        boolean target =
                text.contains("chrome")
             || text.contains("youtube")
             || text.contains("notepad")
             || text.contains("calculator")
             || text.contains("paint")
             || text.contains("explorer")
             || text.contains("tab")
             || text.contains("all");

        return verb && target;
    }

    /* =====================================================
       KNOWLEDGE QUESTION DETECTION
       ===================================================== */
    private static boolean isKnowledgeQuestion(String text) {

        if (text == null) return false;

        text = text.toLowerCase().trim();

        return text.startsWith("what is")
            || text.startsWith("who is")
            || text.startsWith("explain")
            || text.startsWith("tell me about")
            || text.startsWith("how does")
            || text.startsWith("define")
            || text.startsWith("why")
            || text.startsWith("how to");
    }

    /* =====================================================
       MAIN THINK FUNCTION
       ===================================================== */
    public static BrainResult think(String userText) {

        if (userText == null || userText.isBlank()) {
            return null;
        }

        userText = userText.trim();

        /* ---------- COMMAND PATH ---------- */
        if (isSystemCommand(userText)) {
            BrainResult cmd = new BrainResult();
            cmd.type = "command";
            cmd.command = userText;
            return cmd;
        }

        /* ---------- PROMPT SELECTION ---------- */
        boolean knowledge = isKnowledgeQuestion(userText);

        String systemPrompt;

        if (knowledge) {
            systemPrompt = """
                You are TARA, a friendly female voice assistant.
                Explain clearly, step by step.
                Speak naturally like a human teacher.
                Do not rush.
                Avoid unnecessary filler.
                Do not mention being an AI.
                """;
        } else {
            systemPrompt = """
                You are TARA, a friendly female voice assistant.
                Speak naturally like a human.
                Keep replies short unless explanation is required.
                Do not mention being an AI.
                """;
        }

        String fullPrompt =
                systemPrompt +
                "\nUser: " + userText +
                "\nTARA:";

        String jsonRequest = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "stream": false,
                  "options": {
                    "temperature": 0.6,
                    "num_predict": %d,
                    "top_p": 0.9
                  }
                }
                """.formatted(
                        MODEL_NAME,
                        escape(fullPrompt),
                        knowledge ? 420 : 140
                );

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonRequest,
                            StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            if (response.body() == null || response.body().isBlank()) {
                return null;
            }

            OllamaResponse or =
                    gson.fromJson(response.body(), OllamaResponse.class);

            if (or == null || or.response == null || or.response.isBlank()) {
                return null;
            }

            BrainResult result = new BrainResult();
            result.type = "response";
            result.text = clean(or.response);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* =====================================================
       UTILITIES
       ===================================================== */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static String clean(String text) {

        if (text == null) return "";

        return text
                .replaceAll("(?i)tara:", "")
                .replaceAll("\\*+", "")
                .replaceAll("```", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /* =====================================================
       DTOs
       ===================================================== */
    private static class OllamaResponse {
        String response;
    }

    public static class BrainResult {
        public String type;     // "command" or "response"
        public String command;  // raw command text
        public String text;     // LLM output

        public BrainResult() {}

        public BrainResult(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}
