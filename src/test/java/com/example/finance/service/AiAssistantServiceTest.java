package com.example.finance.service;

import com.example.finance.ai.client.AiClient;
import com.example.finance.ai.model.BookQuote;
import com.example.finance.ai.model.FinanceContext;
import com.example.finance.dto.request.AiAssistantQueryRequest;
import com.example.finance.dto.response.AiAssistantResponse;
import com.example.finance.exception.AiProviderException;
import com.example.finance.service.wiki.WikiIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private FinanceContextBuilder financeContextBuilder;

    @Mock
    private PsychologyPrinciplesService psychologyPrinciplesService;

    @Mock
    private BookQuoteRetriever bookQuoteRetriever;

    @Mock
    private QuoteComplianceService quoteComplianceService;

    @Mock
    private AiClient aiClient;

    @Mock
    private WikiIngestionService wikiIngestionService;

    @InjectMocks
    private AiAssistantService aiAssistantService;

    private FinanceContext context;
    private BookQuote quote;

    @BeforeEach
    void setUp() {
        context = FinanceContext.builder().startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 30))
                .totalIncome(new BigDecimal("2000")).totalExpense(new BigDecimal("1200"))
                .netBalance(new BigDecimal("800"))
                .topExpenseCategories(List.of(FinanceContext.CategoryTotal.builder().categoryName("Food")
                        .amount(new BigDecimal("500")).build()))
                .budgets(List.of(FinanceContext.BudgetSnapshot.builder().categoryName("Food")
                        .budgetAmount(new BigDecimal("600")).spentAmount(new BigDecimal("500"))
                        .remainingAmount(new BigDecimal("100")).percentUsed(83.33).build()))
                .creatorAnalysisMonth(YearMonth.of(2026, 4))
                .creatorIncomeBySource(List.of(FinanceContext.CreatorIncomeBySource.builder().source("BRAND")
                        .amount(new BigDecimal("1500")).build()))
                .creatorBaselineIncome(new BigDecimal("1800")).creatorVolatilityBuffer(new BigDecimal("200"))
                .creatorBudgetCommitments(new BigDecimal("400")).creatorSafeToSpend(new BigDecimal("1200")).build();

        quote = BookQuote.builder().sourceTitle("The Psychology of Money").page(52)
                .excerpt("Doing well with money has little to do with how smart you are.").score(0.8).build();
    }

    @Test
    void query_ReturnsAiAnswerAndCitations() {
        AiAssistantQueryRequest request = new AiAssistantQueryRequest();
        request.setQuestion("How do I stop overspending?");
        request.setStartDate(LocalDate.of(2026, 4, 1));
        request.setEndDate(LocalDate.of(2026, 4, 30));

        when(financeContextBuilder.build(1L, request.getStartDate(), request.getEndDate())).thenReturn(context);
        when(psychologyPrinciplesService.principles()).thenReturn(List.of("Behavior beats math"));
        when(psychologyPrinciplesService.strictMentorToneRules()).thenReturn(List.of("Be direct"));
        when(wikiIngestionService.retrieveFromWiki(request.getQuestion(), 3)).thenReturn(List.of());
        when(bookQuoteRetriever.retrieve(request.getQuestion())).thenReturn(List.of(quote));
        when(quoteComplianceService.enforce(any())).thenReturn(List.of(quote));
        when(quoteComplianceService.citations(any())).thenReturn(List.of("The Psychology of Money (p. 52)"));
        when(aiClient.generateAnswer(any(), any())).thenReturn("Cut variable spending by 15% this week.");

        AiAssistantResponse response = aiAssistantService.query(1L, request);

        assertEquals("Cut variable spending by 15% this week.", response.getAnswer());
        assertEquals(1, response.getCitations().size());
        assertTrue(response.getHighlights().stream().anyMatch(s -> s.contains("safe-to-spend")));
    }

    @Test
    void query_FallsBackWhenProviderFails() {
        AiAssistantQueryRequest request = new AiAssistantQueryRequest();
        request.setQuestion("What should I do?");

        when(financeContextBuilder.build(any(), any(), any())).thenReturn(context);
        when(psychologyPrinciplesService.principles()).thenReturn(List.of("Behavior beats math"));
        when(psychologyPrinciplesService.strictMentorToneRules()).thenReturn(List.of("Be direct"));
        when(wikiIngestionService.retrieveFromWiki(request.getQuestion(), 3)).thenReturn(List.of());
        when(bookQuoteRetriever.retrieve(request.getQuestion())).thenReturn(List.of(quote));
        when(quoteComplianceService.enforce(any())).thenReturn(List.of(quote));
        when(quoteComplianceService.citations(any())).thenReturn(List.of("The Psychology of Money (p. 52)"));
        when(aiClient.generateAnswer(any(), any())).thenThrow(new AiProviderException("down"));

        AiAssistantResponse response = aiAssistantService.query(1L, request);

        assertFalse(response.getAnswer().isBlank());
        assertTrue(response.getAnswer().contains("5) Prioritized actions"));
    }
}
