package com.example.finance.service;

import com.example.finance.ai.model.BookQuote;
import com.example.finance.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteComplianceServiceTest {

    private QuoteComplianceService quoteComplianceService;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        properties.setMaxExcerptChars(40);
        properties.setMaxQuotesPerResponse(1);
        quoteComplianceService = new QuoteComplianceService(properties);
    }

    @Test
    void enforce_TruncatesAndLimitsQuotes() {
        BookQuote first = BookQuote.builder().sourceTitle("The Psychology of Money").page(12)
                .excerpt("This is a long excerpt that should be truncated by policy limits.").score(0.9).build();
        BookQuote second = BookQuote.builder().sourceTitle("The Psychology of Money").page(20)
                .excerpt("Second quote should be dropped").score(0.8).build();

        List<BookQuote> output = quoteComplianceService.enforce(List.of(first, second));

        assertEquals(1, output.size());
        assertTrue(output.get(0).getExcerpt().length() <= 40);
    }

    @Test
    void citations_UsesPageFormat() {
        BookQuote quote = BookQuote.builder().sourceTitle("The Psychology of Money").page(33).excerpt("x").score(0.4)
                .build();

        List<String> citations = quoteComplianceService.citations(List.of(quote));

        assertEquals(List.of("The Psychology of Money (p. 33)"), citations);
    }
}
