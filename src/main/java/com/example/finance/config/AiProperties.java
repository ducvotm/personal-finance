package com.example.finance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String provider = "ollama";
    private String baseUrl = "http://localhost:11434";
    private String apiKey = "";
    private String model = "llama3.2";
    private int timeoutSeconds = 20;
    private int maxExcerptChars = 220;
    private int maxQuotesPerResponse = 1;
    private double minRetrievalScore = 0.12;
    private final Wiki wiki = new Wiki();

    private final Book book = new Book();

    @Getter
    @Setter
    public static class Book {
        private String sourcePath = "";
        private String sourceTitle = "The Psychology of Money";
        private int topK = 3;
        private int chunkSize = 800;
        private int chunkOverlap = 120;
        private int embeddingDimensions = 256;
    }

    @Getter
    @Setter
    public static class Wiki {
        private String rootPath = "knowledge";
        private String rawDir = "raw";
        private String wikiDir = "wiki";
        private String logsDir = "logs";
        private int maxQueryPages = 5;
        private int staleAfterDays = 30;
    }
}
