package com.example.finance.service;

import com.example.finance.ai.model.BookChunk;
import com.example.finance.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class BookKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(BookKnowledgeService.class);
    private final AiProperties aiProperties;
    private final AtomicBoolean loaded = new AtomicBoolean(false);
    private volatile List<BookChunk> chunks = List.of();

    public List<BookChunk> getChunks() {
        ensureLoaded();
        return chunks;
    }

    private void ensureLoaded() {
        if (loaded.get()) {
            return;
        }
        synchronized (this) {
            if (loaded.get()) {
                return;
            }
            chunks = loadChunks();
            loaded.set(true);
        }
    }

    private List<BookChunk> loadChunks() {
        String sourcePath = aiProperties.getBook().getSourcePath();
        if (sourcePath == null || sourcePath.isBlank()) {
            log.warn("AI book source path is empty; quote retrieval disabled");
            return List.of();
        }

        File source = new File(sourcePath);
        if (!source.exists()) {
            log.warn("AI book source does not exist at path: {}", sourcePath);
            return List.of();
        }

        try (PDDocument document = Loader.loadPDF(source)) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<BookChunk> output = new ArrayList<>();
            int pageCount = document.getNumberOfPages();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = normalize(stripper.getText(document));
                output.addAll(chunkPage(pageText, page));
            }
            log.info("Loaded {} book chunks from {}", output.size(), sourcePath);
            return Collections.unmodifiableList(output);
        } catch (IOException ex) {
            log.error("Failed to load AI book source: {}", sourcePath, ex);
            return List.of();
        }
    }

    private List<BookChunk> chunkPage(String pageText, int page) {
        if (pageText.isBlank()) {
            return List.of();
        }
        int chunkSize = aiProperties.getBook().getChunkSize();
        int overlap = Math.max(0, aiProperties.getBook().getChunkOverlap());
        int step = Math.max(1, chunkSize - overlap);

        List<BookChunk> output = new ArrayList<>();
        for (int start = 0; start < pageText.length(); start += step) {
            int end = Math.min(pageText.length(), start + chunkSize);
            String text = pageText.substring(start, end).trim();
            if (text.length() < 80) {
                continue;
            }
            String id = "p" + page + "_c" + output.size();
            output.add(BookChunk.builder().id(id).sourceTitle(aiProperties.getBook().getSourceTitle()).page(page)
                    .text(text).embedding(embed(text)).build());
            if (end == pageText.length()) {
                break;
            }
        }
        return output;
    }

    private double[] embed(String text) {
        int dims = aiProperties.getBook().getEmbeddingDimensions();
        double[] vector = new double[dims];
        String[] tokens = normalize(text).split(" ");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int idx = Math.floorMod(token.hashCode(), dims);
            vector[idx] += 1.0;
        }
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            return vector;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
        return vector;
    }

    public double[] embedQuery(String question) {
        return embed(question);
    }

    private String normalize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT).trim();
    }
}
