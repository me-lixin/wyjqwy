package com.wyjqwy.server.service;

import com.wyjqwy.server.mapper.CategoryMapper;
import com.wyjqwy.server.mapper.TemplateMapper;
import com.wyjqwy.server.mapper.TransactionMapper;
import com.wyjqwy.server.model.dto.template.TemplateResponse;
import com.wyjqwy.server.model.entity.CategoryEntity;
import com.wyjqwy.server.model.entity.TemplateEntity;
import com.wyjqwy.server.model.entity.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {
    @Mock
    private TemplateMapper templateMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TemplateService templateService;

    @Test
    void detailUsesFallbackCategoryNameWhenCategoryDeleted() {
        TemplateEntity template = new TemplateEntity();
        template.setId(1L);
        template.setUserId(1L);
        template.setCategoryId(50L);
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(categoryMapper.selectById(50L)).thenReturn(null);

        TemplateResponse response = templateService.detail(1L, 1L);

        assertEquals("已删除分类", response.categoryName());
    }

    @Test
    void applyCreatesTransactionFromTemplate() {
        TemplateEntity template = new TemplateEntity();
        template.setId(2L);
        template.setUserId(1L);
        template.setType(1);
        template.setAmount(new BigDecimal("25.50"));
        template.setCategoryId(3L);
        template.setNote("午餐");
        when(templateMapper.selectById(2L)).thenReturn(template);

        templateService.apply(1L, 2L);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionMapper).insert(captor.capture());
        TransactionEntity saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(1, saved.getType());
        assertEquals(new BigDecimal("25.50"), saved.getAmount());
        assertEquals(3L, saved.getCategoryId());
        assertEquals("午餐", saved.getNote());
        assertNotNull(saved.getOccurredAt());
    }

    @Test
    void detailUsesRealCategoryWhenExists() {
        TemplateEntity template = new TemplateEntity();
        template.setId(3L);
        template.setUserId(1L);
        template.setCategoryId(8L);
        CategoryEntity category = new CategoryEntity();
        category.setId(8L);
        category.setName("通勤");
        category.setIcon("bus");
        when(templateMapper.selectById(3L)).thenReturn(template);
        when(categoryMapper.selectById(8L)).thenReturn(category);

        TemplateResponse response = templateService.detail(1L, 3L);

        assertEquals("通勤", response.categoryName());
        assertEquals("bus", response.categoryIcon());
    }
}
