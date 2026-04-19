package com.example.finance.service.wiki;

import com.example.finance.ai.model.BookQuote;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WikiIngestionService {

    private static final DateTimeFormatter LOG_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern TOKEN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]{3,}");

    private final WikiPaths wikiPaths;
    private final WikiBootstrapService wikiBootstrapService;

    public WikiIngestionService(WikiPaths wikiPaths, WikiBootstrapService wikiBootstrapService) {
        this.wikiPaths = wikiPaths;
        this.wikiBootstrapService = wikiBootstrapService;
    }

    public synchronized IngestSummary ingestPendingSources() {
        wikiBootstrapService.ensureStructure();
        Properties state = readState();
        List<Path> candidates = listRawFiles();
        List<String> ingested = new ArrayList<>();
        List<String> createdPages = new ArrayList<>();
        List<String> updatedPages = new ArrayList<>();

        for (Path source : candidates) {
            String fingerprint = fingerprint(source);
            String key = source.toAbsolutePath().normalize().toString();
            if (fingerprint.equals(state.getProperty(key))) {
                continue;
            }
            String text = readText(source);
            if (text.isBlank()) {
                state.setProperty(key, fingerprint);
                continue;
            }
            List<String> keywords = topKeywords(text, 3);
            String slug = slug(source.getFileName().toString());
            Path wikiPage = wikiPaths.wikiDir().resolve(slug + ".md");
            boolean exists = Files.exists(wikiPage);
            writeWikiPage(wikiPage, source, text, keywords);
            if (exists) {
                updatedPages.add("wiki/" + wikiPage.getFileName());
            } else {
                createdPages.add("wiki/" + wikiPage.getFileName());
            }
            ingested.add("raw/" + source.getFileName());
            state.setProperty(key, fingerprint);
        }

        writeState(state);
        if (!ingested.isEmpty()) {
            rewriteIndex();
            appendLog("ingest", "files=" + ingested.size() + ", created=" + createdPages.size() + ", updated=" +
                    updatedPages.size());
        }
        return new IngestSummary(ingested, createdPages, updatedPages);
    }

    public List<BookQuote> retrieveFromWiki(String question, int limit) {
        wikiBootstrapService.ensureStructure();
        List<PageMatch> matches = new ArrayList<>();
        Set<String> terms = questionTerms(question);
        for (Path page : listWikiPages()) {
            String content = readSafe(page);
            if (content.isBlank()) {
                continue;
            }
            int score = score(content, terms);
            if (score <= 0) {
                continue;
            }
            String excerpt = summarizeExcerpt(content, 280);
            String citation = "wiki/" + page.getFileName() + "#Summary";
            matches.add(new PageMatch(score,
                    BookQuote.builder().sourceTitle(citation).page(1).excerpt(excerpt).score(score).build()));
        }
        matches.sort(Comparator.comparingInt(PageMatch::score).reversed());
        List<BookQuote> output = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, matches.size()); i++) {
            output.add(matches.get(i).quote());
        }
        return output;
    }

    public synchronized String lintWiki() {
        wikiBootstrapService.ensureStructure();
        List<Path> pages = listWikiPages();
        List<String> warnings = new ArrayList<>();
        Set<String> pageNames = new HashSet<>();
        Set<String> inbound = new HashSet<>();

        for (Path page : pages) {
            pageNames.add(page.getFileName().toString());
            String content = readSafe(page);
            if (!content.contains("[") || !content.contains("]")) {
                warnings.add("warning: missing citations in " + page.getFileName());
            }
            Matcher linkMatcher = Pattern.compile("\\[\\[([^\\]]+)\\]\\]").matcher(content);
            while (linkMatcher.find()) {
                String target = normalizePageName(linkMatcher.group(1));
                inbound.add(target);
            }
            if (!content.toLowerCase(Locale.ROOT).contains("## contradictions")) {
                warnings.add("info: contradictions section missing in " + page.getFileName());
            }
            LocalDate staleBefore = LocalDate.now().minusDays(30);
            Matcher dateMatcher = Pattern.compile("LastUpdated:\\s*(\\d{4}-\\d{2}-\\d{2})").matcher(content);
            if (dateMatcher.find()) {
                LocalDate updated = LocalDate.parse(dateMatcher.group(1));
                if (updated.isBefore(staleBefore)) {
                    warnings.add("warning: stale page " + page.getFileName());
                }
            } else {
                warnings.add("warning: LastUpdated metadata missing in " + page.getFileName());
            }
        }
        for (String name : pageNames) {
            if (!inbound.contains(name)) {
                warnings.add("warning: orphan page " + name);
            }
        }

        String date = LocalDate.now().toString();
        Path report = wikiPaths.logsDir().resolve("lint-" + date + ".md");
        StringBuilder body = new StringBuilder();
        body.append("# Wiki Lint Report ").append(date).append("\n\n");
        if (warnings.isEmpty()) {
            body.append("- info: no issues found\n");
        } else {
            for (String warning : warnings) {
                body.append("- ").append(warning).append("\n");
            }
        }
        writeString(report, body.toString());
        appendLog("lint", "issues=" + warnings.size() + ", report=" + report.getFileName());
        return report.toString();
    }

    private void writeWikiPage(Path wikiPage, Path source, String text, List<String> keywords) {
        String summary = summarizeExcerpt(text, 520);
        StringBuilder content = new StringBuilder();
        content.append("# ").append(titleFrom(source)).append("\n\n");
        content.append("## Summary\n").append(summary).append("\n\n");
        content.append("## Key Facts\n");
        if (keywords.isEmpty()) {
            content.append("- Insufficient extracted keywords [raw/").append(source.getFileName())
                    .append("#chunk-1]\n\n");
        } else {
            for (String keyword : keywords) {
                content.append("- ").append(keyword).append(" appears relevant [raw/").append(source.getFileName())
                        .append("#chunk-1]\n");
            }
            content.append("\n");
        }
        content.append("## Evidence\n");
        content.append("- ").append(summarizeExcerpt(text, 300)).append(" [raw/").append(source.getFileName())
                .append("#chunk-1]\n\n");
        content.append("## Related Pages\n- None\n\n");
        content.append("## Contradictions\n- None detected\n\n");
        content.append("## Open Questions\n- What additional source should corroborate this summary?\n\n");
        content.append("## Metadata\n");
        content.append("- LastUpdated: ").append(LocalDate.now()).append("\n");
        content.append("- Confidence: medium\n");
        writeString(wikiPage, content.toString());
    }

    private void rewriteIndex() {
        List<Path> pages = listWikiPages();
        StringBuilder index = new StringBuilder();
        index.append("# Wiki Index\n\n");
        index.append("Auto-maintained catalog of wiki pages.\n\n");
        for (Path page : pages) {
            String content = readSafe(page);
            String summary = "No summary";
            Matcher matcher = Pattern.compile("## Summary\\s*(.+?)(\\n##|\\z)", Pattern.DOTALL).matcher(content);
            if (matcher.find()) {
                summary = matcher.group(1).replaceAll("\\s+", " ").trim();
            }
            index.append("- [").append(page.getFileName()).append("](./").append(page.getFileName()).append("): ")
                    .append(truncate(summary, 160)).append("\n");
        }
        writeString(wikiPaths.indexFile(), index.toString());
    }

    private void appendLog(String operation, String details) {
        String ts = LocalDateTime.now().format(LOG_TS);
        String entry = "## [" + ts + "] " + operation + " | " + details + "\n";
        try {
            Files.writeString(wikiPaths.logFile(), entry, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to append wiki log", ex);
        }
    }

    private Properties readState() {
        Properties props = new Properties();
        Path file = wikiPaths.ingestStateFile();
        if (Files.exists(file)) {
            try (var in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read ingest state", ex);
            }
        }
        return props;
    }

    private void writeState(Properties props) {
        try (var out = Files.newOutputStream(wikiPaths.ingestStateFile())) {
            props.store(out, "Ingested source fingerprints");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write ingest state", ex);
        }
    }

    private List<Path> listRawFiles() {
        try (var stream = Files.list(wikiPaths.rawDir())) {
            return stream.filter(Files::isRegularFile).filter(p -> !p.getFileName().toString().startsWith(".")).sorted()
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to list raw sources", ex);
        }
    }

    private List<Path> listWikiPages() {
        try (var stream = Files.list(wikiPaths.wikiDir())) {
            return stream.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".md"))
                    .filter(p -> !"index.md".equals(p.getFileName().toString())).sorted().toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to list wiki pages", ex);
        }
    }

    private String readText(Path source) {
        String name = source.getFileName().toString().toLowerCase(Locale.ROOT);
        try {
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = Loader.loadPDF(source.toFile())) {
                    return new PDFTextStripper().getText(doc);
                }
            }
            return Files.readString(source, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private String readSafe(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private String fingerprint(Path path) {
        try {
            return Files.size(path) + ":" + Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            return "0:0";
        }
    }

    private void writeString(Path file, String content) {
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed writing " + file, ex);
        }
    }

    private List<String> topKeywords(String text, int n) {
        Matcher matcher = TOKEN.matcher(text.toLowerCase(Locale.ROOT));
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words.stream().distinct().limit(n).toList();
    }

    private Set<String> questionTerms(String question) {
        Matcher matcher = TOKEN.matcher(question.toLowerCase(Locale.ROOT));
        Set<String> terms = new HashSet<>();
        while (matcher.find()) {
            terms.add(matcher.group());
        }
        return terms;
    }

    private int score(String content, Set<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        String lc = content.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            if (lc.contains(term)) {
                score++;
            }
        }
        return score;
    }

    private String summarizeExcerpt(String text, int maxLen) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLen) {
            return normalized;
        }
        return normalized.substring(0, maxLen - 3).trim() + "...";
    }

    private String slug(String name) {
        String noExt = name.replaceAll("\\.[a-zA-Z0-9]+$", "");
        String slug = noExt.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "source" : slug;
    }

    private String titleFrom(Path source) {
        String fileName = source.getFileName().toString().replaceAll("\\.[a-zA-Z0-9]+$", "");
        return fileName.replace('-', ' ');
    }

    private String truncate(String value, int maxLen) {
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen - 3) + "...";
    }

    private String normalizePageName(String wikiLink) {
        String candidate = wikiLink.trim();
        if (!candidate.endsWith(".md")) {
            candidate = candidate + ".md";
        }
        return candidate;
    }

    private record PageMatch(int score, BookQuote quote) {
    }

    public record IngestSummary(List<String> filesIngested, List<String> pagesCreated, List<String> pagesUpdated) {
    }
}
