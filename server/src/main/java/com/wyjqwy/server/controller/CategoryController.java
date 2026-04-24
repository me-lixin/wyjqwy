package com.wyjqwy.server.controller;

import com.wyjqwy.server.common.ApiResponse;
import com.wyjqwy.server.model.dto.category.CategoryDeleteResult;
import com.wyjqwy.server.model.dto.category.CategoryMigrateRequest;
import com.wyjqwy.server.model.dto.category.CategoryUpsertRequest;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.security.SecurityUtils;
import com.wyjqwy.server.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryEntity>> list(@RequestParam(required = false) Integer type) {
        return ApiResponse.ok(categoryService.list(SecurityUtils.currentUserId(), type));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CategoryUpsertRequest request) {
        categoryService.create(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpsertRequest request) {
        categoryService.update(SecurityUtils.currentUserId(), id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CategoryDeleteResult> delete(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.tryDelete(SecurityUtils.currentUserId(), id));
    }

    @PostMapping("/{id}/migrate-and-delete")
    public ApiResponse<Void> migrateAndDelete(@PathVariable Long id, @Valid @RequestBody CategoryMigrateRequest request) {
        categoryService.migrateAndDelete(SecurityUtils.currentUserId(), id, request.targetCategoryId());
        return ApiResponse.ok();
    }
}
