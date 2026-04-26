package com.wyjqwy.server.controller;

import com.wyjqwy.server.common.ApiResponse;
import com.wyjqwy.server.model.dto.stats.CategoryStatResponse;
import com.wyjqwy.server.model.dto.stats.StatsSummaryResponse;
import com.wyjqwy.server.model.dto.stats.TrendPointResponse;
import com.wyjqwy.server.security.SecurityUtils;
import com.wyjqwy.server.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ApiResponse<StatsSummaryResponse> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(statsService.summary(SecurityUtils.currentUserId(), from, to));
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendPointResponse>> trend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "day") String granularity) {
        return ApiResponse.ok(statsService.trend(SecurityUtils.currentUserId(), from, to, type, granularity));
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CategoryStatResponse>> byCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam Integer type,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(statsService.byCategory(SecurityUtils.currentUserId(), from, to, type, limit));
    }
}
