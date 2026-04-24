package com.wyjqwy.server.controller;

import com.wyjqwy.server.common.ApiResponse;
import com.wyjqwy.server.model.dto.common.PageResponse;
import com.wyjqwy.server.model.dto.transaction.TransactionResponse;
import com.wyjqwy.server.model.dto.transaction.TransactionUpsertRequest;
import com.wyjqwy.server.security.SecurityUtils;
import com.wyjqwy.server.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> list(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(transactionService.list(SecurityUtils.currentUserId(), from, to, type, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(transactionService.detail(SecurityUtils.currentUserId(), id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody TransactionUpsertRequest request) {
        transactionService.create(SecurityUtils.currentUserId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TransactionUpsertRequest request) {
        transactionService.update(SecurityUtils.currentUserId(), id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        transactionService.delete(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok();
    }
}
