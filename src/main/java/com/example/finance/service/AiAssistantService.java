package com.example.finance.service;

import com.example.finance.ai.client.AiClient;
import com.example.finance.ai.model.BookQuote;
import com.example.finance.ai.model.FinanceContext;
import com.example.finance.dto.request.AiAssistantQueryRequest;
import com.example.finance.dto.response.AiAssistantResponse;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.AiProviderException;
import com.example.finance.service.wiki.WikiIngestionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    private final FinanceContextBuilder financeContextBuilder;
    private final PsychologyPrinciplesService psychologyPrinciplesService;
    private final BookQuoteRetriever bookQuoteRetriever;
    private final QuoteComplianceService quoteComplianceService;
    private final AiClient aiClient;
    private final WikiIngestionService wikiIngestionService;

    @Transactional(readOnly = true)
    public AiAssistantResponse query(Long userId, AiAssistantQueryRequest request) {
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minusDays(30);
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }

        FinanceContext context = financeContextBuilder.build(userId, startDate, endDate);
        wikiIngestionService.ingestPendingSources();
        List<BookQuote> quotes = quoteComplianceService.enforce(mergeQuotes(request.getQuestion()));
        List<String> citations = quoteComplianceService.citations(quotes);

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request.getQuestion(), context, quotes);

        String answer;
        try {
            answer = aiClient.generateAnswer(systemPrompt, userPrompt);
        } catch (AiProviderException ex) {
            log.warn("AI provider unavailable for user {}: {}", userId, ex.getMessage());
            answer = fallbackAnswer(context, quotes);
        }

        return AiAssistantResponse.builder().answer(answer).usedStartDate(startDate).usedEndDate(endDate)
                .highlights(buildHighlights(context)).citations(citations).build();
    }

    private String buildSystemPrompt() {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a personal financial analyst for independent creators with irregular income.\n");
        builder.append("Optimize for volatile cash flow: feast months, famine months, tax set-asides, and runway.\n");
        builder.append("Speak like an analyst briefing a client: structured, precise, no hype.\n");
        builder.append("Only use the provided user financial context and retrieved book/wiki excerpts.\n");
        builder.append("If data is missing or thin, say so and say what the user should log next.\n");
        builder.append("Do not invent numbers, dates, or account details.\n");
        builder.append("Do not recommend specific securities, funds, or crypto to buy or sell.\n");
        builder.append(
                "Do not provide legal or tax advice; you may remind the user to consult a qualified professional.\n");
        builder.append("Format every answer with these sections (use the exact headings):\n");
        builder.append("1) Executive summary — 2-3 sentences.\n");
        builder.append("2) Data used — bullet list referencing only figures from the context.\n");
        builder.append("3) Creator cash-flow view — income mix, volatility, runway implications.\n");
        builder.append("4) Risks and assumptions — what could break the picture.\n");
        builder.append("5) Prioritized actions — 3 numbered steps for the next 7-14 days.\n");
        builder.append("Behavioral lenses (use when relevant, not as filler):\n");
        for (String principle : psychologyPrinciplesService.principles()) {
            builder.append("- ").append(principle).append("\n");
        }
        builder.append("Tone rules:\n");
        for (String rule : psychologyPrinciplesService.strictMentorToneRules()) {
            builder.append("- ").append(rule).append("\n");
        }
        return builder.toString();
    }

    private String buildUserPrompt(String question, FinanceContext context, List<BookQuote> quotes) {
        StringBuilder builder = new StringBuilder();
        builder.append("User question: ").append(question).append("\n");
        builder.append("Reporting window: ").append(context.getStartDate()).append(" to ").append(context.getEndDate())
                .append("\n");
        builder.append("Totals in window — income: ").append(context.getTotalIncome()).append(", expense: ")
                .append(context.getTotalExpense()).append(", net: ").append(context.getNetBalance()).append("\n");
        builder.append("Creator month metrics (calendar month ").append(context.getCreatorAnalysisMonth())
                .append("):\n");
        builder.append("- Safe-to-spend (app estimate): ").append(context.getCreatorSafeToSpend()).append("\n");
        builder.append("- Baseline income (trailing average): ").append(context.getCreatorBaselineIncome())
                .append("\n");
        builder.append("- Volatility buffer: ").append(context.getCreatorVolatilityBuffer()).append("\n");
        builder.append("- Budget commitments in month: ").append(context.getCreatorBudgetCommitments()).append("\n");
        builder.append("Creator income by source in that month:\n");
        if (context.getCreatorIncomeBySource().isEmpty()) {
            builder.append("- (none logged with incomeSource for this month)\n");
        } else {
            for (FinanceContext.CreatorIncomeBySource row : context.getCreatorIncomeBySource()) {
                builder.append("- ").append(row.getSource()).append(": ").append(row.getAmount()).append("\n");
            }
        }
        builder.append("Top expense categories in reporting window:\n");
        for (FinanceContext.CategoryTotal category : context.getTopExpenseCategories()) {
            builder.append("- ").append(category.getCategoryName()).append(": ").append(category.getAmount())
                    .append("\n");
        }
        builder.append("Budget status in reporting window:\n");
        for (FinanceContext.BudgetSnapshot budget : context.getBudgets()) {
            builder.append("- ").append(budget.getCategoryName()).append(" budget=").append(budget.getBudgetAmount())
                    .append(", spent=").append(budget.getSpentAmount()).append(", remaining=")
                    .append(budget.getRemainingAmount()).append(", used=").append(budget.getPercentUsed())
                    .append("%\n");
        }
        if (!quotes.isEmpty()) {
            builder.append("Book excerpts:\n");
            for (BookQuote quote : quotes) {
                builder.append("- ").append(quote.getSourceTitle()).append(" (p. ").append(quote.getPage())
                        .append("): ").append(quote.getExcerpt()).append("\n");
            }
        }
        builder.append("When using excerpts, cite as Source (p. XX).");
        return builder.toString();
    }

    private List<String> buildHighlights(FinanceContext context) {
        List<String> highlights = new ArrayList<>();
        highlights.add("Creator month " + context.getCreatorAnalysisMonth() + " — safe-to-spend: " +
                context.getCreatorSafeToSpend());
        highlights.add("Window net (income minus expense): " + context.getNetBalance());
        if (!context.getCreatorIncomeBySource().isEmpty()) {
            FinanceContext.CreatorIncomeBySource top = context.getCreatorIncomeBySource().stream()
                    .max((a, b) -> a.getAmount().compareTo(b.getAmount())).orElseThrow();
            highlights.add("Largest income source this month: " + top.getSource() + " (" + top.getAmount() + ")");
        } else {
            highlights.add("No tagged creator income (BRAND/ADS/AFFILIATE/PRODUCT) for analysis month.");
        }
        if (!context.getTopExpenseCategories().isEmpty()) {
            FinanceContext.CategoryTotal top = context.getTopExpenseCategories().get(0);
            highlights.add("Top expense in window: " + top.getCategoryName() + " (" + top.getAmount() + ")");
        }
        long overBudget = context.getBudgets().stream().filter(b -> b.getPercentUsed() >= 100.0).count();
        highlights.add("Over-budget categories in window: " + overBudget);
        return highlights;
    }

    private List<BookQuote> mergeQuotes(String question) {
        List<BookQuote> rawQuotes = bookQuoteRetriever.retrieve(question);
        List<BookQuote> wikiQuotes = wikiIngestionService.retrieveFromWiki(question, 3);
        List<BookQuote> merged = new ArrayList<>(wikiQuotes.size() + rawQuotes.size());
        merged.addAll(wikiQuotes);
        merged.addAll(rawQuotes);
        return merged;
    }

    private String fallbackAnswer(FinanceContext context, List<BookQuote> quotes) {
        StringBuilder builder = new StringBuilder();
        builder.append("1) Executive summary — ");
        builder.append("In ").append(context.getCreatorAnalysisMonth()).append(" the app estimates safe-to-spend at ")
                .append(context.getCreatorSafeToSpend()).append(" after volatility and budget commitments. ");
        builder.append("Over ").append(context.getStartDate()).append("–").append(context.getEndDate())
                .append(", net cash flow is ").append(context.getNetBalance()).append(". ");
        if (context.getNetBalance().signum() < 0) {
            builder.append("You are drawing down faster than income in the window; treat this as a runway risk. ");
        } else {
            builder.append("You are net positive in the window; keep building buffer for low-income months. ");
        }
        builder.append("\n2) Data used — safe-to-spend components: baseline ")
                .append(context.getCreatorBaselineIncome()).append(", volatility buffer ")
                .append(context.getCreatorVolatilityBuffer()).append(", budget commitments ")
                .append(context.getCreatorBudgetCommitments()).append(".\n");
        builder.append("3) Creator cash-flow view — ");
        if (context.getCreatorIncomeBySource().isEmpty()) {
            builder.append("Log income with incomeSource (BRAND, ADS, AFFILIATE, PRODUCT) to see concentration risk. ");
        } else {
            builder.append("Income mix this month: ");
            context.getCreatorIncomeBySource()
                    .forEach(row -> builder.append(row.getSource()).append("=").append(row.getAmount()).append("; "));
        }
        builder.append("\n4) Risks and assumptions — ");
        builder.append("Safe-to-spend uses trailing months; a sudden drop in deals is not fully predicted.\n");
        builder.append("5) Prioritized actions — ");
        builder.append("(1) Tag every payout with incomeSource. ");
        builder.append("(2) Hold one month of core expenses outside day-to-day spending. ");
        builder.append("(3) Reconcile budgets weekly until volatility feels under control.");
        if (!quotes.isEmpty()) {
            BookQuote quote = quotes.get(0);
            builder.append(" Reference: \"").append(quote.getExcerpt()).append("\" (").append(quote.getSourceTitle())
                    .append(" p. ").append(quote.getPage()).append(").");
        }
        return builder.toString();
    }
}
