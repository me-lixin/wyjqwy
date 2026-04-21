package com.wyjqwy.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.mapper.CategoryMapper;
import com.wyjqwy.server.mapper.TransactionMapper;
import com.wyjqwy.server.model.dto.common.PageResponse;
import com.wyjqwy.server.model.dto.transaction.TransactionResponse;
import com.wyjqwy.server.model.dto.transaction.TransactionUpsertRequest;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;

    public TransactionService(TransactionMapper transactionMapper, CategoryMapper categoryMapper) {
        this.transactionMapper = transactionMapper;
        this.categoryMapper = categoryMapper;
    }

    public PageResponse<TransactionResponse> list(Long userId, LocalDateTime from, LocalDateTime to, Integer type, long page, long size) {
        Page<TransactionEntity> pageQuery = new Page<>(page, size);
        Page<TransactionEntity> result = transactionMapper.selectPage(pageQuery, new LambdaQueryWrapper<TransactionEntity>()
                .eq(TransactionEntity::getUserId, userId)
                .eq(type != null, TransactionEntity::getType, type)
                .ge(from != null, TransactionEntity::getOccurredAt, from)
                .lt(to != null, TransactionEntity::getOccurredAt, to)
                .orderByDesc(TransactionEntity::getOccurredAt)
                .orderByDesc(TransactionEntity::getId));
        List<TransactionResponse> records = result.getRecords().stream().map(this::toResponse).toList();
        return new PageResponse<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    public TransactionResponse detail(Long userId, Long id) {
        return toResponse(mustGetOwned(userId, id));
    }

    public void create(Long userId, TransactionUpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        TransactionEntity entity = new TransactionEntity();
        entity.setUserId(userId);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCategoryId(request.categoryId());
        entity.setNote(request.note());
        entity.setOccurredAt(request.occurredAt());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        transactionMapper.insert(entity);
    }

    public void update(Long userId, Long id, TransactionUpsertRequest request) {
        TransactionEntity entity = mustGetOwned(userId, id);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCategoryId(request.categoryId());
        entity.setNote(request.note());
        entity.setOccurredAt(request.occurredAt());
        entity.setUpdatedAt(LocalDateTime.now());
        transactionMapper.updateById(entity);
    }

    public void delete(Long userId, Long id) {
        mustGetOwned(userId, id);
        transactionMapper.deleteById(id);
    }

    private TransactionEntity mustGetOwned(Long userId, Long id) {
        TransactionEntity entity = transactionMapper.selectById(id);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException("transaction not found");
        }
        return entity;
    }

    private TransactionResponse toResponse(TransactionEntity entity) {
        CategoryEntity category = categoryMapper.selectById(entity.getCategoryId());
        String categoryName = category == null ? "已删除分类" : category.getName();
        String categoryIcon = category == null ? null : category.getIcon();
        return new TransactionResponse(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCategoryId(),
                categoryName,
                categoryIcon,
                entity.getNote(),
                entity.getOccurredAt()
        );
    }
}
