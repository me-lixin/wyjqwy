package com.wyjqwy.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.mapper.CategoryMapper;
import com.wyjqwy.server.mapper.TransactionMapper;
import com.wyjqwy.server.model.dto.category.CategoryDeleteResult;
import com.wyjqwy.server.model.dto.category.CategoryUpsertRequest;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;

    public CategoryService(CategoryMapper categoryMapper, TransactionMapper transactionMapper) {
        this.categoryMapper = categoryMapper;
        this.transactionMapper = transactionMapper;
    }

    public List<CategoryEntity> list(Long userId, Integer type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .and(w -> w.eq(CategoryEntity::getUserId, 0L).or().eq(CategoryEntity::getUserId, userId))
                .eq(type != null, CategoryEntity::getType, type)
                .orderByDesc(CategoryEntity::getUserId)
                .orderByDesc(CategoryEntity::getSort)
                .orderByAsc(CategoryEntity::getId));
    }

    public void create(Long userId, CategoryUpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        CategoryEntity entity = new CategoryEntity();
        entity.setUserId(userId);
        entity.setType(request.type());
        entity.setName(request.name());
        entity.setIcon(request.icon());
        entity.setSort(request.sort() == null ? 0 : request.sort());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        categoryMapper.insert(entity);
    }

    public void update(Long userId, Long id, CategoryUpsertRequest request) {
        CategoryEntity entity = mustGetOwned(userId, id);
        entity.setType(request.type());
        entity.setName(request.name());
        entity.setIcon(request.icon());
        entity.setSort(request.sort() == null ? 0 : request.sort());
        entity.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(entity);
    }

    /**
     * 尝试删除：无关联账单则删除并返回 deleted=true；否则不删除，返回 pendingTransactionCount。
     */
    public CategoryDeleteResult tryDelete(Long userId, Long id) {
        mustGetOwned(userId, id);
        long pending = countTransactionsForCategory(userId, id);
        if (pending > 0) {
            return new CategoryDeleteResult(false, pending);
        }
        categoryMapper.deleteById(id);
        return new CategoryDeleteResult(true, 0);
    }

    private long countTransactionsForCategory(Long userId, Long categoryId) {
        return transactionMapper.selectCount(new LambdaQueryWrapper<TransactionEntity>()
                .eq(TransactionEntity::getUserId, userId)
                .eq(TransactionEntity::getCategoryId, categoryId));
    }

    /**
     * 将源分类下账单批量改到目标分类（须同类型），再删除源分类。
     */
    @Transactional
    public void migrateAndDelete(Long userId, Long sourceCategoryId, Long targetCategoryId) {
        if (sourceCategoryId.equals(targetCategoryId)) {
            throw new BizException("target must differ from source");
        }
        CategoryEntity source = mustGetOwned(userId, sourceCategoryId);
        CategoryEntity target = mustGetVisibleCategory(userId, targetCategoryId);
        if (!source.getType().equals(target.getType())) {
            throw new BizException("category type mismatch");
        }
        LambdaUpdateWrapper<TransactionEntity> uw = new LambdaUpdateWrapper<TransactionEntity>()
                .eq(TransactionEntity::getUserId, userId)
                .eq(TransactionEntity::getCategoryId, sourceCategoryId)
                .set(TransactionEntity::getCategoryId, targetCategoryId)
                .set(TransactionEntity::getUpdatedAt, LocalDateTime.now());
        transactionMapper.update(null, uw);
        categoryMapper.deleteById(sourceCategoryId);
    }

    /**
     * 系统分类（userId=0）或当前用户自建分类。
     */
    private CategoryEntity mustGetVisibleCategory(Long userId, Long id) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BizException("category not found");
        }
        Long uid = entity.getUserId();
        long owner = uid == null ? 0L : uid;
        if (owner != 0L && !entity.getUserId().equals(userId)) {
            throw new BizException("category not found");
        }
        return entity;
    }

    private CategoryEntity mustGetOwned(Long userId, Long id) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null || !userId.equals(entity.getUserId())) {
            throw new BizException("category not found");
        }
        return entity;
    }
}
