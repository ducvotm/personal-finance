package com.example.finance.service;

import com.example.finance.ai.model.BookQuote;
import com.example.finance.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteComplianceService {

    private final AiProperties aiProperties;

    public List<BookQuote> enforce(List<BookQuote> quotes) {
        int maxQuotes = Math.max(0, aiProperties.getMaxQuotesPerResponse());
        int maxChars = Math.max(1, aiProperties.getMaxExcerptChars());

        List<BookQuote> sanitized = new ArrayList<>();
        for (BookQuote quote : quotes) {
            if (sanitized.size() >= maxQuotes) {
                break;
            }
            String excerpt = trim(quote.getExcerpt(), maxChars);
            sanitized.add(BookQuote.builder().sourceTitle(quote.getSourceTitle()).page(quote.getPage())
                    .score(quote.getScore()).excerpt(excerpt).build());
        }
        return sanitized;
    }

    public List<String> citations(List<BookQuote> quotes) {
        return quotes.stream().map(quote -> quote.getSourceTitle() + " (p. " + quote.getPage() + ")").distinct()
                .toList();
    }

    private String trim(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars - 3).trim() + "...";
    }
}
