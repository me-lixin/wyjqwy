package com.wyjqwy.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.mapper.CategoryMapper;
import com.wyjqwy.server.mapper.TemplateMapper;
import com.wyjqwy.server.mapper.TransactionMapper;
import com.wyjqwy.server.model.dto.common.PageResponse;
import com.wyjqwy.server.model.dto.template.TemplateResponse;
import com.wyjqwy.server.model.dto.template.TemplateUpsertRequest;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.model.entity.TemplateEntity;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemplateService {
    private final TemplateMapper templateMapper;
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;

    public TemplateService(TemplateMapper templateMapper, CategoryMapper categoryMapper, TransactionMapper transactionMapper) {
        this.templateMapper = templateMapper;
        this.categoryMapper = categoryMapper;
        this.transactionMapper = transactionMapper;
    }

    public PageResponse<TemplateResponse> list(Long userId, Integer type, long page, long size) {
        Page<TemplateEntity> pageQuery = new Page<>(page, size);
        Page<TemplateEntity> result = templateMapper.selectPage(pageQuery, new LambdaQueryWrapper<TemplateEntity>()
                .eq(TemplateEntity::getUserId, userId)
                .eq(type != null, TemplateEntity::getType, type)
                .orderByDesc(TemplateEntity::getSort)
                .orderByAsc(TemplateEntity::getId));
        List<TemplateResponse> records = result.getRecords().stream().map(this::toResponse).toList();
        return new PageResponse<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    public TemplateResponse detail(Long userId, Long id) {
        return toResponse(mustGetOwned(userId, id));
    }

    public void create(Long userId, TemplateUpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        TemplateEntity entity = new TemplateEntity();
        entity.setUserId(userId);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCategoryId(request.categoryId());
        entity.setNote(request.note());
        entity.setSort(request.sort() == null ? 0 : request.sort());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        templateMapper.insert(entity);
    }

    public void update(Long userId, Long id, TemplateUpsertRequest request) {
        TemplateEntity entity = mustGetOwned(userId, id);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCategoryId(request.categoryId());
        entity.setNote(request.note());
        entity.setSort(request.sort() == null ? 0 : request.sort());
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    public void delete(Long userId, Long id) {
        mustGetOwned(userId, id);
        templateMapper.deleteById(id);
    }

    public void apply(Long userId, Long id) {
        TemplateEntity template = mustGetOwned(userId, id);
        LocalDateTime now = LocalDateTime.now();
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setUserId(userId);
        transactionEntity.setType(template.getType());
        transactionEntity.setAmount(template.getAmount());
        transactionEntity.setCategoryId(template.getCategoryId());
        transactionEntity.setNote(template.getNote());
        transactionEntity.setOccurredAt(now);
        transactionEntity.setCreatedAt(now);
        transactionEntity.setUpdatedAt(now);
        transactionMapper.insert(transactionEntity);
    }

    private TemplateEntity mustGetOwned(Long userId, Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException("template not found");
        }
        return entity;
    }

    private TemplateResponse toResponse(TemplateEntity entity) {
        CategoryEntity category = categoryMapper.selectById(entity.getCategoryId());
        String categoryName = category == null ? "已删除分类" : category.getName();
        String categoryIcon = category == null ? null : category.getIcon();
        return new TemplateResponse(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCategoryId(),
                categoryName,
                categoryIcon,
                entity.getNote(),
                entity.getSort()
        );
    }
}
