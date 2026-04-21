package com.wyjqwy.server.model.dto.category;

/**
 * DELETE /api/categories/{id}：若无关联账单则已删除；若有则未删除并返回待处理笔数。
 */
public record CategoryDeleteResult(boolean deleted, long pendingTransactionCount) {}
