package com.example.finance.ai.client;

import com.example.finance.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class AiClientProviderSelectionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PropertyPlaceholderAutoConfiguration.class,
                    ConfigurationPropertiesAutoConfiguration.class, RestTemplateAutoConfiguration.class))
            .withBean(AiProperties.class).withBean(OpenAiClient.class).withBean(OllamaClient.class);

    @Test
    void usesOpenAiClientByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AiClient.class);
            assertThat(context.getBean(AiClient.class)).isInstanceOf(OpenAiClient.class);
        });
    }

    @Test
    void usesOpenAiClientWhenProviderIsOpenAi() {
        contextRunner.withPropertyValues("ai.provider=openai").run(context -> {
            assertThat(context).hasSingleBean(AiClient.class);
            assertThat(context.getBean(AiClient.class)).isInstanceOf(OpenAiClient.class);
        });
    }

    @Test
    void usesOllamaClientWhenProviderIsOllama() {
        contextRunner.withPropertyValues("ai.provider=ollama").run(context -> {
            assertThat(context).hasSingleBean(AiClient.class);
            assertThat(context.getBean(AiClient.class)).isInstanceOf(OllamaClient.class);
        });
    }
}
