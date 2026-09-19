package com.fortunecloud.resumeai.service;

import com.fortunecloud.resumeai.exception.AiNotConfiguredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over an OpenAI-compatible Chat Completions endpoint.
 * Compatible with OpenAI, Mistral, Groq, Together and local Ollama.
 */
@Service
public class AiClient {
	
private static final Logger log = LoggerFactory.getLogger(AiClient.class);
    private final RestClient client;
    private final String apiKey;
    private final String model;

    public AiClient(RestClient aiRestClient,
                    @Value("${ai.api.key}") String apiKey,
                    @Value("${ai.api.model}") String model) {
        this.client = aiRestClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    /** True when an API key is present and real AI generation is possible. */
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * Sends a system + user prompt and returns the assistant's text.
     *
     * @throws AiNotConfiguredException when no API key is set (caller falls back to templates)
     */
    @SuppressWarnings("unchecked")
    public String complete(String systemPrompt, String userPrompt) {
        if (!isConfigured()) {
            throw new AiNotConfiguredException("AI_API_KEY is not set");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.6,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        try {
            Map<String, Object> response = client.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("choices")) {
                throw new IllegalStateException("Empty response from AI provider");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return ((String) message.get("content")).trim();

        } catch (AiNotConfiguredException e) {
            throw e;
        } catch (Exception e) {
            log.warn("AI call failed, will fall back to template: {}", e.getMessage());
            throw new AiNotConfiguredException("AI call failed: " + e.getMessage());
        }
    }
}
