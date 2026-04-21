package com.wyjqwy.app.data

import java.time.LocalDateTime

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String)
data class TokenRefreshRequest(val refreshToken: String)
data class AuthTokenResponse(val accessToken: String, val refreshToken: String)

data class Category(
    val id: Long,
    val type: Int,
    val name: String,
    val icon: String?,
    val sort: Int,
    /** 后端：0 为系统分类，非 0 为用户自建 */
    val userId: Long? = null
) {
    val isUserCategory: Boolean
        get() = userId != null && userId != 0L
}

data class CategoryUpsertRequest(
    val type: Int,
    val name: String,
    val icon: String?,
    val sort: Int
)

/** DELETE /api/categories/{id}：deleted=true 表示已删除；否则 pendingTransactionCount 为待迁移笔数 */
data class CategoryDeleteResult(
    val deleted: Boolean,
    val pendingTransactionCount: Long
)

data class CategoryMigrateRequest(
    val targetCategoryId: Long
)

data class TransactionItem(
    val id: Long,
    val type: Int,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String?,
    val note: String?,
    val occurredAt: String,
    /** 在 ViewModel 拉取或本地构造时写入，避免 UI 在渲染路径反复解析字符串 */
    val parsedOccurredAt: LocalDateTime? = null
)

data class TransactionUpsertRequest(
    val type: Int,
    val amount: Double,
    val categoryId: Long,
    val note: String?,
    val occurredAt: String
)

data class TemplateItem(
    val id: Long,
    val type: Int,
    val amount: Double?,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String?,
    val note: String?,
    val sort: Int
)

data class TemplateUpsertRequest(
    val type: Int,
    val amount: Double?,
    val categoryId: Long,
    val note: String?,
    val sort: Int
)

data class PageResponse<T>(
    val page: Long,
    val size: Long,
    val total: Long,
    val records: List<T>
)

data class StatsSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)
