package com.example.finance.ai.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookQuote {
    String sourceTitle;
    int page;
    String excerpt;
    double score;
}
