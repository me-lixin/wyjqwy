package com.wyjqwy.server.service;

import com.wyjqwy.server.mapper.StatsMapper;
import com.wyjqwy.server.model.dto.stats.CategoryStatResponse;
import com.wyjqwy.server.model.dto.stats.StatsSummaryResponse;
import com.wyjqwy.server.model.dto.stats.TrendPointResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsService {
    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public StatsSummaryResponse summary(Long userId, LocalDateTime from, LocalDateTime to) {
        return statsMapper.summary(userId, from, to);
    }

    public List<TrendPointResponse> trend(Long userId, LocalDateTime from, LocalDateTime to, Integer type, String granularity) {
        return statsMapper.trend(userId, from, to, type, granularity);
    }

    public List<CategoryStatResponse> byCategory(Long userId, LocalDateTime from, LocalDateTime to, Integer type, Integer limit) {
        return statsMapper.byCategory(userId, from, to, type, limit == null ? 10 : limit);
    }
}
