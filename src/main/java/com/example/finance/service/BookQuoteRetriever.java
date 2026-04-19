package com.example.finance.service;

import com.example.finance.ai.model.BookChunk;
import com.example.finance.ai.model.BookQuote;
import com.example.finance.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookQuoteRetriever {

    private final BookKnowledgeService bookKnowledgeService;
    private final AiProperties aiProperties;

    public List<BookQuote> retrieve(String question) {
        List<BookChunk> chunks = bookKnowledgeService.getChunks();
        if (chunks.isEmpty()) {
            return List.of();
        }

        double[] queryEmbedding = bookKnowledgeService.embedQuery(question);
        return chunks.stream().map(chunk -> toQuote(chunk, cosine(queryEmbedding, chunk.getEmbedding())))
                .filter(quote -> quote.getScore() >= aiProperties.getMinRetrievalScore())
                .sorted(Comparator.comparingDouble(BookQuote::getScore).reversed())
                .limit(aiProperties.getBook().getTopK()).toList();
    }

    private BookQuote toQuote(BookChunk chunk, double score) {
        return BookQuote.builder().sourceTitle(chunk.getSourceTitle()).page(chunk.getPage()).excerpt(chunk.getText())
                .score(score).build();
    }

    private double cosine(double[] left, double[] right) {
        double dot = 0.0;
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            dot += left[i] * right[i];
        }
        return dot;
    }
}
