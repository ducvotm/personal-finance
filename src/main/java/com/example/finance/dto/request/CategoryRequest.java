package com.example.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category creation/update request payload")
public class CategoryRequest {

    @Schema(description = "Category name", example = "Groceries")
    @NotBlank(message = "Category name is required")
    private String name;

    @Schema(description = "Category type (INCOME or EXPENSE)", example = "EXPENSE")
    @NotBlank(message = "Category type is required")
    private String type;

    @Schema(description = "Icon identifier", example = "shopping-cart")
    private String icon;

    @Schema(description = "Color hex code", example = "#FF5733")
    private String color;
}
