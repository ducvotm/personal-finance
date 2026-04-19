package com.example.finance.service.wiki;

import com.example.finance.config.AiProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikiIngestionServiceTest {

    private Path tempRoot;

    @AfterEach
    void tearDown() throws IOException {
        if (tempRoot != null && Files.exists(tempRoot)) {
            try (var walk = Files.walk(tempRoot)) {
                walk.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    void ingestPendingSources_CreatesWikiPageIndexAndLog() throws IOException {
        tempRoot = Files.createTempDirectory("wiki-test");
        AiProperties props = new AiProperties();
        props.getWiki().setRootPath(tempRoot.toString());
        WikiPaths paths = new WikiPaths(props);
        WikiBootstrapService bootstrapService = new WikiBootstrapService(paths);
        WikiIngestionService service = new WikiIngestionService(paths, bootstrapService);

        bootstrapService.ensureStructure();
        Files.writeString(paths.rawDir().resolve("tokyo.md"),
                "Tokyo budget planning should prioritize accommodation near transit and food areas.",
                StandardCharsets.UTF_8);

        WikiIngestionService.IngestSummary summary = service.ingestPendingSources();

        assertFalse(summary.filesIngested().isEmpty());
        assertTrue(Files.exists(paths.wikiDir().resolve("tokyo.md")));
        assertTrue(Files.readString(paths.indexFile()).contains("tokyo.md"));
        assertTrue(Files.readString(paths.logFile()).contains("ingest"));
    }

    @Test
    void lintWiki_WritesLintReport() throws IOException {
        tempRoot = Files.createTempDirectory("wiki-lint-test");
        AiProperties props = new AiProperties();
        props.getWiki().setRootPath(tempRoot.toString());
        WikiPaths paths = new WikiPaths(props);
        WikiBootstrapService bootstrapService = new WikiBootstrapService(paths);
        WikiIngestionService service = new WikiIngestionService(paths, bootstrapService);

        bootstrapService.ensureStructure();
        Files.writeString(paths.wikiDir().resolve("sample.md"),
                "# Sample\n\n## Summary\nTest summary.\n\n## Metadata\n- LastUpdated: 2020-01-01\n",
                StandardCharsets.UTF_8);

        String report = service.lintWiki();

        assertTrue(Files.exists(Path.of(report)));
        String reportBody = Files.readString(Path.of(report));
        assertTrue(reportBody.contains("warning"));
        assertTrue(Files.readString(paths.logFile()).contains("lint"));
    }
}
