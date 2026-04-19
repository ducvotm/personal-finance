package com.example.finance.dto.response;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class AiAssistantResponse {
    String answer;
    LocalDate usedStartDate;
    LocalDate usedEndDate;
    List<String> highlights;
    List<String> citations;
}
