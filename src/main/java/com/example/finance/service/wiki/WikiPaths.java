package com.example.finance.service.wiki;

import com.example.finance.config.AiProperties;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Component
public class WikiPaths {

    private final AiProperties aiProperties;

    public WikiPaths(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public Path root() {
        return Path.of(aiProperties.getWiki().getRootPath());
    }

    public Path rawDir() {
        return root().resolve(aiProperties.getWiki().getRawDir());
    }

    public Path wikiDir() {
        return root().resolve(aiProperties.getWiki().getWikiDir());
    }

    public Path logsDir() {
        return root().resolve(aiProperties.getWiki().getLogsDir());
    }

    public Path indexFile() {
        return wikiDir().resolve("index.md");
    }

    public Path logFile() {
        return logsDir().resolve("log.md");
    }

    public Path ingestStateFile() {
        return logsDir().resolve("ingest-state.properties");
    }
}
