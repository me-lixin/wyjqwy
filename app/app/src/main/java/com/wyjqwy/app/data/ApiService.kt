package com.wyjqwy.app.data

import com.wyjqwy.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<Void>

    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<AuthTokenResponse>

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body body: TokenRefreshRequest): ApiResponse<AuthTokenResponse>

    @GET("/api/categories")
    suspend fun getCategories(
        @Header("Authorization") auth: String,
        @Query("type") type: Int? = null
    ): ApiResponse<List<Category>>

    @POST("/api/categories")
    suspend fun createCategory(
        @Header("Authorization") auth: String,
        @Body body: CategoryUpsertRequest
    ): ApiResponse<Void>

    @PUT("/api/categories/{id}")
    suspend fun updateCategory(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body body: CategoryUpsertRequest
    ): ApiResponse<Void>

    @DELETE("/api/categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): ApiResponse<CategoryDeleteResult>

    @POST("/api/categories/{id}/migrate-and-delete")
    suspend fun migrateAndDeleteCategory(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body body: CategoryMigrateRequest
    ): ApiResponse<Void>

    @GET("/api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") auth: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("type") type: Int? = null,
        @Query("page") page: Long = 1,
        @Query("size") size: Long = 200
    ): ApiResponse<PageResponse<TransactionItem>>

    @POST("/api/transactions")
    suspend fun createTransaction(
        @Header("Authorization") auth: String,
        @Body body: TransactionUpsertRequest
    ): ApiResponse<Long>

    @POST("/api/transactions/voice")
    suspend fun createTransactionByVoice(
        @Header("Authorization") auth: String,
        @Body body: VoiceTransactionRequest
    ): ApiResponse<VoiceTransactionResult>

    @PUT("/api/transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body body: TransactionUpsertRequest
    ): ApiResponse<Void>

    @DELETE("/api/transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): ApiResponse<Void>

    @GET("/api/templates")
    suspend fun getTemplates(
        @Header("Authorization") auth: String,
        @Query("page") page: Long = 1,
        @Query("size") size: Long = 20
    ): ApiResponse<PageResponse<TemplateItem>>

    @POST("/api/templates")
    suspend fun createTemplate(
        @Header("Authorization") auth: String,
        @Body body: TemplateUpsertRequest
    ): ApiResponse<Void>

    @DELETE("/api/templates/{id}")
    suspend fun deleteTemplate(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): ApiResponse<Void>

    @POST("/api/templates/{id}/apply")
    suspend fun applyTemplate(
        @Header("Authorization") auth: String,
        @Path("id") id: Long
    ): ApiResponse<Void>

    @GET("/api/stats/summary")
    suspend fun getSummary(
        @Header("Authorization") auth: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ApiResponse<StatsSummary>
}

object ApiClient {
    // 可通过 Gradle 属性 SERVER_BASE_URL 覆盖（如真机调试 IP）
    private val BASE_URL = BuildConfig.SERVER_BASE_URL
    private const val VOICE_TIMEOUT_SECONDS = 180

    val service: ApiService by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val perApiTimeoutInterceptor = okhttp3.Interceptor { chain ->
            val req = chain.request()
            if (req.url.encodedPath.endsWith("/api/transactions/voice")) {
                chain
                    .withConnectTimeout(VOICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .withReadTimeout(VOICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .withWriteTimeout(VOICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .proceed(req)
            } else {
                chain.proceed(req)
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(perApiTimeoutInterceptor)
            .addInterceptor(logger)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
