package com.example.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AiAssistantQueryRequest {
    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must be at most 500 characters")
    private String question;

    private LocalDate startDate;

    private LocalDate endDate;
}
