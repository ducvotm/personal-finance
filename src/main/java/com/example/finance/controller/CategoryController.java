package com.example.finance.controller;

import com.example.finance.dto.request.CategoryRequest;
import com.example.finance.dto.response.ApiResponse;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", description = "Creates a new transaction category")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")})
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Valid @RequestBody
    CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", response));
    }

    @Operation(summary = "Get all categories", description = "Retrieves all categories, optionally filtered by type (INCOME/EXPENSE)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(@AuthenticationPrincipal
    UserPrincipal userPrincipal,
            @Parameter(description = "Filter by category type (INCOME or EXPENSE)") @RequestParam(required = false)
            String type) {
        List<CategoryResponse> categories;
        if (type != null) {
            categories = categoryService.getCategoriesByType(userPrincipal.getId(), type);
        } else {
            categories = categoryService.getAllCategories(userPrincipal.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Category ID") @PathVariable
    Long id) {
        CategoryResponse response = categoryService.getCategoryById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update category", description = "Updates an existing category")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Category ID") @PathVariable
    Long id, @Valid @RequestBody
    CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @Operation(summary = "Delete category", description = "Deletes a category")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Category ID") @PathVariable
    Long id) {
        categoryService.deleteCategory(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
