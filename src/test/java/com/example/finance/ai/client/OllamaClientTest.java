package com.example.finance.ai.client;

import com.example.finance.config.AiProperties;
import com.example.finance.exception.AiProviderException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class OllamaClientTest {

    @Test
    void generateAnswer_ReturnsTrimmedContent() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:11434/api/chat")).andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"message\":{\"content\":\"  Stay within budget this week.  \"}}",
                        MediaType.APPLICATION_JSON));

        AiProperties properties = new AiProperties();
        properties.setProvider("ollama");
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("llama3.2");

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.setConnectTimeout(any())).thenReturn(builder);
        when(builder.setReadTimeout(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        OllamaClient client = new OllamaClient(properties, builder);

        String answer = client.generateAnswer("system", "user");

        assertEquals("Stay within budget this week.", answer);
        server.verify();
    }

    @Test
    void generateAnswer_ThrowsWhenMessageMissing() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("http://localhost:11434/api/chat")).andExpect(method(POST))
                .andRespond(withSuccess("{\"done\":true}", MediaType.APPLICATION_JSON));

        AiProperties properties = new AiProperties();
        properties.setProvider("ollama");
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("llama3.2");

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.setConnectTimeout(any())).thenReturn(builder);
        when(builder.setReadTimeout(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        OllamaClient client = new OllamaClient(properties, builder);

        assertThrows(AiProviderException.class, () -> client.generateAnswer("system", "user"));
        server.verify();
    }
}
