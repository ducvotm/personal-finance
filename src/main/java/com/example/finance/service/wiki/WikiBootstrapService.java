package com.example.finance.service.wiki;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class WikiBootstrapService {

    private final WikiPaths wikiPaths;

    public WikiBootstrapService(WikiPaths wikiPaths) {
        this.wikiPaths = wikiPaths;
    }

    public void ensureStructure() {
        try {
            Files.createDirectories(wikiPaths.rawDir());
            Files.createDirectories(wikiPaths.wikiDir());
            Files.createDirectories(wikiPaths.logsDir());
            if (Files.notExists(wikiPaths.indexFile())) {
                Files.writeString(wikiPaths.indexFile(), "# Wiki Index\n\nAuto-maintained catalog of wiki pages.\n",
                        StandardCharsets.UTF_8);
            }
            if (Files.notExists(wikiPaths.logFile())) {
                Files.writeString(wikiPaths.logFile(),
                        "# Wiki Operation Log\n\nAppend-only timeline of ingest/query/lint operations.\n",
                        StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize wiki directory structure", ex);
        }
    }
}
