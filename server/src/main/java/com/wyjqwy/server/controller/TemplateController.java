package com.wyjqwy.server.controller;

import com.wyjqwy.server.common.ApiResponse;
import com.wyjqwy.server.model.dto.common.PageResponse;
import com.wyjqwy.server.model.dto.template.TemplateResponse;
import com.wyjqwy.server.model.dto.template.TemplateUpsertRequest;
import com.wyjqwy.server.security.SecurityUtils;
import com.wyjqwy.server.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TemplateResponse>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(templateService.list(SecurityUtils.currentUserId(), type, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(templateService.detail(SecurityUtils.currentUserId(), id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody TemplateUpsertRequest request) {
        templateService.create(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TemplateUpsertRequest request) {
        templateService.update(SecurityUtils.currentUserId(), id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        templateService.delete(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/apply")
    public ApiResponse<Void> apply(@PathVariable Long id) {
        templateService.apply(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok();
    }
}
