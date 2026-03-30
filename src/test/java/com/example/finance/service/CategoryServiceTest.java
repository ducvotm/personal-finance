package com.example.finance.service;

import com.example.finance.dto.request.CategoryRequest;
import com.example.finance.dto.response.CategoryResponse;
import com.example.finance.entity.Category;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").email("test@example.com").password("password")
                .createdAt(LocalDateTime.now()).build();

        testCategory = Category.builder().id(1L).name("Food").type("EXPENSE").icon("🍔").color("#FF5722").user(testUser)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        categoryRequest = CategoryRequest.builder().name("Food").type("EXPENSE").icon("🍔").color("#FF5722").build();
    }

    @Test
    void createCategory_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.createCategory(1L, categoryRequest);

        assertNotNull(response);
        assertEquals("Food", response.getName());
        assertEquals("EXPENSE", response.getType());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> 
            categoryService.createCategory(1L, categoryRequest));
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));

        CategoryResponse response = categoryService.getCategoryById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Food", response.getName());
    }

    @Test
    void getCategoryById_NotFound() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            categoryService.getCategoryById(1L, 1L));
    }

    @Test
    void getAllCategories_Success() {
        Category category2 = Category.builder().id(2L).name("Transport").type("EXPENSE").user(testUser)
                .createdAt(LocalDateTime.now()).build();

        when(categoryRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCategory, category2));

        List<CategoryResponse> responses = categoryService.getAllCategories(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void getCategoriesByType_Success() {
        when(categoryRepository.findByUserIdAndType(1L, "EXPENSE"))
                .thenReturn(Arrays.asList(testCategory));

        List<CategoryResponse> responses = categoryService.getCategoriesByType(1L, "EXPENSE");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("EXPENSE", responses.get(0).getType());
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest updateRequest = CategoryRequest.builder().name("Food & Drinks").type("EXPENSE").icon("🍕")
                .color("#FF0000").build();

        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndUserId("Food & Drinks", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(1L, 1L, updateRequest);

        assertNotNull(response);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(any(Category.class));

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L, 1L));
        verify(categoryRepository, times(1)).delete(testCategory);
    }
}
