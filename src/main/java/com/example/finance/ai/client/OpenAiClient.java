package com.example.finance.ai.client;

import com.example.finance.config.AiProperties;
import com.example.finance.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiClient implements AiClient {

    private final AiProperties aiProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public String generateAnswer(String systemPrompt, String userPrompt) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new AiProviderException("AI provider API key is not configured");
        }

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .setReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds())).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        Map<String, Object> body = Map.of("model", aiProperties.getModel(), "temperature", 0.2, "messages", List
                .of(Map.of("role", "system", "content", systemPrompt), Map.of("role", "user", "content", userPrompt)));

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(aiProperties.getBaseUrl() + "/chat/completions",
                    new HttpEntity<>(body, headers), Map.class);

            Object choicesObj = response.getBody() != null ? response.getBody().get("choices") : null;
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                throw new AiProviderException("AI provider response is missing choices");
            }

            Object firstChoice = choices.get(0);
            if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
                throw new AiProviderException("AI provider response choice is invalid");
            }

            Object messageObj = choiceMap.get("message");
            if (!(messageObj instanceof Map<?, ?> messageMap)) {
                throw new AiProviderException("AI provider response message is invalid");
            }

            Object contentObj = messageMap.get("content");
            if (!(contentObj instanceof String content) || content.isBlank()) {
                throw new AiProviderException("AI provider returned an empty answer");
            }

            return content.trim();
        } catch (RestClientException ex) {
            throw new AiProviderException("AI provider request failed", ex);
        }
    }
}
