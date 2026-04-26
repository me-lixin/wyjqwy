package com.wyjqwy.server.service;

import com.wyjqwy.server.mapper.CategoryMapper;
import com.wyjqwy.server.mapper.TransactionMapper;
import com.wyjqwy.server.model.dto.transaction.TransactionResponse;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void detailUsesFallbackCategoryNameWhenCategoryDeleted() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(10L);
        entity.setUserId(1L);
        entity.setCategoryId(99L);
        when(transactionMapper.selectById(10L)).thenReturn(entity);
        when(categoryMapper.selectById(99L)).thenReturn(null);

        TransactionResponse response = transactionService.detail(1L, 10L);

        assertEquals("已删除分类", response.categoryName());
        assertNull(response.categoryIcon());
    }

    @Test
    void detailUsesRealCategoryNameWhenCategoryExists() {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(11L);
        entity.setUserId(1L);
        entity.setCategoryId(1L);
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("餐饮");
        category.setIcon("food");
        when(transactionMapper.selectById(11L)).thenReturn(entity);
        when(categoryMapper.selectById(1L)).thenReturn(category);

        TransactionResponse response = transactionService.detail(1L, 11L);

        assertEquals("餐饮", response.categoryName());
        assertEquals("food", response.categoryIcon());
    }
}
