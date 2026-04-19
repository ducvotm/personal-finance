package com.example.finance.ai.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookChunk {
    String id;
    String sourceTitle;
    int page;
    String text;
    double[] embedding;
}
