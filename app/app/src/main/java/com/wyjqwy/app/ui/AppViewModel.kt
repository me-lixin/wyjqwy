package com.wyjqwy.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wyjqwy.app.data.*
import com.wyjqwy.app.ui.invest.AutoInvestState
import com.wyjqwy.app.ui.stats.ChartStatsState
import com.wyjqwy.app.ui.util.parseOccurredAtToLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class AppUiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val message: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** 明细页：是否显示金额（眼睛） */
    val amountVisible: Boolean = true,
    /** 明细/图表等：当前查看的月份 */
    val selectedYearMonth: YearMonth = YearMonth.now(),
    /** 支出分类 type=1 */
    val categoriesExpense: List<Category> = emptyList(),
    /** 收入分类 type=2 */
    val categoriesIncome: List<Category> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val templates: List<TemplateItem> = emptyList(),
    val summary: StatsSummary? = null,
    val loginPhone: String = "",
    /** 我的页总览统计（首次通过接口全量加载，后续本地缓存增量维护） */
    val overviewTotalDays: Int? = null,
    val overviewTotalCount: Int? = null,
    val overviewLoading: Boolean = false
)

class AppViewModel(
    private val api: ApiService,
    private val sessionStore: SessionStore,
    private val preferencesStore: PreferencesStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    /** 图表页：UI + 按年缓存，离开页面不丢失 */
    private val _chartStats = MutableStateFlow(ChartStatsState())
    val chartStats: StateFlow<ChartStatsState> = _chartStats.asStateFlow()

    /** 定投页：近年投资明细缓存 */
    private val _autoInvest = MutableStateFlow(AutoInvestState())
    val autoInvest: StateFlow<AutoInvestState> = _autoInvest.asStateFlow()
    private var overviewTransactionsCache: List<TransactionItem>? = null

    fun setChartBillType(type: Int) {
        _chartStats.value = _chartStats.value.copy(billType = type)
    }

    fun setChartPeriodOrdinal(ordinal: Int) {
        _chartStats.value = _chartStats.value.copy(periodOrdinal = ordinal.coerceIn(0, 2))
    }

    fun setChartYearMonth(year: Int, month: Int) {
        _chartStats.value = _chartStats.value.copy(
            selectedYear = year,
            selectedMonth = month.coerceIn(1, 12)
        )
    }

    fun setChartYearWeek(year: Int, week: Int) {
        _chartStats.value = _chartStats.value.copy(selectedYear = year, selectedWeek = week)
    }

    fun setChartYearOnly(year: Int) {
        _chartStats.value = _chartStats.value.copy(selectedYear = year)
    }

    /**
     * 按自然年拉取并缓存；已缓存的年份不会重复请求。
     */
    fun ensureChartYearsLoaded(years: Set<Int>) {
        if (years.isEmpty()) return
        viewModelScope.launch {
            years.forEach { year ->
                if (_chartStats.value.yearTransactions.containsKey(year)) return@forEach
                if (_chartStats.value.loadingYears.contains(year)) return@forEach
                loadChartYear(year)
            }
        }
    }

    private suspend fun loadChartYear(year: Int) {
        _chartStats.value = _chartStats.value.copy(
            loadingYears = _chartStats.value.loadingYears + year,
            lastError = null
        )
        try {
            val from = LocalDate.of(year, 1, 1).atStartOfDay()
            val to = from.plusYears(1)
            val list = fetchTransactionsForRange(from, to)
            _chartStats.value = _chartStats.value.copy(
                yearTransactions = _chartStats.value.yearTransactions + (year to list),
                loadingYears = _chartStats.value.loadingYears - year
            )
        } catch (e: Exception) {
            _chartStats.value = _chartStats.value.copy(
                loadingYears = _chartStats.value.loadingYears - year,
                lastError = formatApiError(e)
            )
        }
    }

    private fun removeTransactionFromChartCaches(transactionId: Long) {
        val m = _chartStats.value.yearTransactions.mapValues { (_, list) ->
            list.filterNot { it.id == transactionId }
        }
        _chartStats.value = _chartStats.value.copy(yearTransactions = m)
    }

    private fun upsertTransactionIntoChartCaches(tx: TransactionItem) {
        val year = tx.parsedOccurredAt?.year ?: return
        val current = _chartStats.value
        val existing = current.yearTransactions[year] ?: return
        val merged = (listOf(tx) + existing.filterNot { it.id == tx.id })
            .sortedByDescending { it.parsedOccurredAt ?: LocalDateTime.MIN }
        _chartStats.value = current.copy(
            yearTransactions = current.yearTransactions + (year to merged)
        )
    }

    private fun clearChartStats() {
        _chartStats.value = ChartStatsState()
    }

    fun ensureAutoInvestYearsLoaded(years: Set<Int>) {
        if (years.isEmpty()) return
        viewModelScope.launch {
            years.forEach { year ->
                if (_autoInvest.value.yearTransactions.containsKey(year)) return@forEach
                if (_autoInvest.value.loadingYears.contains(year)) return@forEach
                loadAutoInvestYear(year)
            }
        }
    }

    private suspend fun loadAutoInvestYear(year: Int) {
        _autoInvest.value = _autoInvest.value.copy(
            loadingYears = _autoInvest.value.loadingYears + year,
            lastError = null
        )
        try {
            val from = LocalDate.of(year, 1, 1).atStartOfDay()
            val to = from.plusYears(1)
            val list = fetchTransactionsForRange(from, to)
            _autoInvest.value = _autoInvest.value.copy(
                yearTransactions = _autoInvest.value.yearTransactions + (year to list),
                loadingYears = _autoInvest.value.loadingYears - year
            )
        } catch (e: Exception) {
            _autoInvest.value = _autoInvest.value.copy(
                loadingYears = _autoInvest.value.loadingYears - year,
                lastError = formatApiError(e)
            )
        }
    }

    private fun removeTransactionFromAutoInvestCaches(transactionId: Long) {
        val m = _autoInvest.value.yearTransactions.mapValues { (_, list) ->
            list.filterNot { it.id == transactionId }
        }
        _autoInvest.value = _autoInvest.value.copy(yearTransactions = m)
    }

    private fun upsertTransactionIntoAutoInvestCaches(tx: TransactionItem) {
        val year = tx.parsedOccurredAt?.year ?: return
        val current = _autoInvest.value
        val existing = current.yearTransactions[year] ?: return
        val merged = (listOf(tx) + existing.filterNot { it.id == tx.id })
            .sortedByDescending { it.parsedOccurredAt ?: LocalDateTime.MIN }
        _autoInvest.value = current.copy(
            yearTransactions = current.yearTransactions + (year to merged)
        )
    }

    private fun clearAutoInvestState() {
        _autoInvest.value = AutoInvestState()
    }

    fun ensureOverviewStatsLoaded() = viewModelScope.launch {
        if (!_uiState.value.loggedIn) return@launch
        if (overviewTransactionsCache != null) return@launch
        try {
            _uiState.value = _uiState.value.copy(overviewLoading = true)
            val all = fetchAllTransactionsForOverview()
            overviewTransactionsCache = all
            val (days, count) = computeOverviewStats(all)
            _uiState.value = _uiState.value.copy(
                overviewTotalDays = days,
                overviewTotalCount = count,
                overviewLoading = false
            )
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(overviewLoading = false)
        }
    }

    init {
        viewModelScope.launch {
            preferencesStore.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
        val hasAccess = !sessionStore.getAccessToken().isNullOrBlank()
        val hasRefresh = !sessionStore.getRefreshToken().isNullOrBlank()
        _uiState.value = _uiState.value.copy(
            loggedIn = hasAccess || hasRefresh,
            loginPhone = sessionStore.getLoginPhone().orEmpty()
        )
        if (hasAccess) {
            loadHomeData()
        } else if (hasRefresh) {
            viewModelScope.launch {
                try {
                    refreshAccessTokenOrThrow()
                    _uiState.value = _uiState.value.copy(loggedIn = true)
                    loadHomeData()
                } catch (_: Exception) {
                    sessionStore.clear()
                    _uiState.value = _uiState.value.copy(loggedIn = false)
                }
            }
        }
    }

    fun setAmountVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(amountVisible = visible)
    }

    fun setSelectedYearMonth(ym: YearMonth) {
        loadTransactionsForMonth(ym)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        preferencesStore.setThemeMode(mode)
    }

    fun clearUiMessage() {
        _uiState.value = _uiState.value.copy(message = "")
    }

    fun register(username: String, password: String) = viewModelScope.launch {
        runAction {
            api.register(RegisterRequest(username, password))
            "注册成功，请登录"
        }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        runAction {
            val res = api.login(LoginRequest(username, password))
            val token = requireNotNull(res.data) { "登录响应为空" }
            sessionStore.saveTokens(token.accessToken, token.refreshToken)
            sessionStore.saveLoginPhone(username)
            _uiState.value = _uiState.value.copy(loggedIn = true)
            loadHomeData()
            _uiState.value = _uiState.value.copy(loginPhone = username)
            "登录成功"
        }
    }

    fun logout() {
        sessionStore.clear()
        val theme = _uiState.value.themeMode
        clearChartStats()
        clearAutoInvestState()
        overviewTransactionsCache = null
        _uiState.value = AppUiState(loggedIn = false, message = "已退出登录", themeMode = theme)
    }

    fun loadHomeData() = viewModelScope.launch {
        runAction {
            val auth = authHeader()
            val ym = _uiState.value.selectedYearMonth
            val (tx, summary) = fetchMonthData(auth, ym)
            val categoriesExpense = api.getCategories(auth, 1).data.orEmpty()
            val categoriesIncome = api.getCategories(auth, 2).data.orEmpty()
            val templates = api.getTemplates(auth, 1, 20).data?.records.orEmpty()
            _uiState.value = _uiState.value.copy(
                selectedYearMonth = ym,
                transactions = tx,
                categoriesExpense = categoriesExpense,
                categoriesIncome = categoriesIncome,
                templates = templates,
                summary = summary
            )
            "数据已刷新"
        }
    }

    /**
     * 静默刷新：不改 loading，避免在轻量操作后出现全屏/页面级转圈。
     */
    fun loadHomeDataQuietly() = viewModelScope.launch {
        try {
            val auth = authHeader()
            val ym = _uiState.value.selectedYearMonth
            val (tx, summary) = fetchMonthData(auth, ym)
            val categoriesExpense = api.getCategories(auth, 1).data.orEmpty()
            val categoriesIncome = api.getCategories(auth, 2).data.orEmpty()
            val templates = api.getTemplates(auth, 1, 20).data?.records.orEmpty()
            _uiState.value = _uiState.value.copy(
                selectedYearMonth = ym,
                transactions = tx,
                categoriesExpense = categoriesExpense,
                categoriesIncome = categoriesIncome,
                templates = templates,
                summary = summary
            )
        } catch (_: Exception) {
            // 静默刷新失败不打断当前交互，后续用户操作会触发正常刷新。
        }
    }

    fun navigateMonthWindow(delta: Int) {
        val target = _uiState.value.selectedYearMonth.plusMonths(delta.toLong())
        if (delta > 0 && target > YearMonth.now()) return
        loadTransactionsForMonth(target)
    }

    fun loadTransactionsForMonth(ym: YearMonth) = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(loading = true, message = "")
            val auth = authHeader()
            val (tx, summary) = fetchMonthData(auth, ym)
            _uiState.value = _uiState.value.copy(
                loading = false,
                selectedYearMonth = ym,
                transactions = tx,
                summary = summary
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                message = "请求失败: ${formatApiError(e)}"
            )
        }
    }

    fun addCategory(type: Int, name: String, iconKey: String) = viewModelScope.launch {
        runAction {
            api.createCategory(
                authHeader(),
                CategoryUpsertRequest(type = type, name = name, icon = iconKey, sort = 0)
            )
            loadHomeData()
            "分类已新增"
        }
    }

    fun updateCategory(categoryId: Long, type: Int, name: String, iconKey: String) = viewModelScope.launch {
        runAction {
            api.updateCategory(
                authHeader(),
                categoryId,
                CategoryUpsertRequest(type = type, name = name, icon = iconKey, sort = 0)
            )
            loadHomeData()
            "分类已更新"
        }
    }

    /**
     * 尝试删除：服务端若无关联账单则已删；否则 [CategoryDeleteResult.deleted] 为 false，并返回待迁移笔数。
     */
    suspend fun tryDeleteCategory(categoryId: Long): CategoryDeleteResult {
        return callWithTokenRefreshOnce {
            val res = api.deleteCategory(authHeader(), categoryId)
            ensureOk(res)
            res.data ?: CategoryDeleteResult(deleted = true, pendingTransactionCount = 0)
        }
    }

    fun migrateAndDeleteCategory(
        sourceCategoryId: Long,
        targetCategoryId: Long,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(loading = true, message = "")
            callWithTokenRefreshOnce {
                val res = api.migrateAndDeleteCategory(
                    authHeader(),
                    sourceCategoryId,
                    CategoryMigrateRequest(targetCategoryId = targetCategoryId)
                )
                ensureOk(res)
            }
            loadHomeData()
            _uiState.value = _uiState.value.copy(loading = false, message = "账单已迁移并删除分类")
            onSuccess()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                message = "请求失败: ${formatApiError(e)}"
            )
        }
    }

    fun deleteTransaction(transactionId: Long) = viewModelScope.launch {
        try {
            callWithTokenRefreshOnce {
                val res = api.deleteTransaction(authHeader(), transactionId)
                ensureOk(res)
            }
            val merged = _uiState.value.transactions.filterNot { it.id == transactionId }
            removeTransactionFromChartCaches(transactionId)
            removeTransactionFromAutoInvestCaches(transactionId)
            overviewTransactionsCache = overviewTransactionsCache?.filterNot { it.id == transactionId }
            _uiState.value = _uiState.value.copy(
                transactions = merged,
                summary = recomputeSummary(merged),
                message = "明细已删除"
            )
            syncOverviewFromCacheIfPresent()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(message = "请求失败: ${formatApiError(e)}")
        }
    }

    /**
     * 从分类选择页记账：若无同名分类则先创建再记一笔。
     */
    fun addTransactionAfterCategory(
        type: Int,
        categoryName: String,
        iconKey: String,
        amount: Double,
        note: String,
        occurredAt: LocalDateTime = LocalDateTime.now(),
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(loading = true, message = "")
            var categoryId = 0L
            callWithTokenRefreshOnce {
                categoryId = ensureCategoryId(type, categoryName, iconKey)
                val createRes = api.createTransaction(
                    authHeader(),
                    TransactionUpsertRequest(
                        type = type,
                        amount = amount,
                        categoryId = categoryId,
                        note = note.ifBlank { null },
                        occurredAt = formatDateTime(occurredAt)
                    )
                )
                ensureOk(createRes)
            }
            _uiState.value = _uiState.value.copy(loading = false, message = "记账成功")
            onSuccess()
            val current = _uiState.value
            val localTx = TransactionItem(
                id = -System.currentTimeMillis(),
                type = type,
                amount = amount,
                categoryId = categoryId,
                categoryName = categoryName,
                categoryIcon = iconKey,
                note = note.ifBlank { null },
                occurredAt = formatDateTime(occurredAt),
                parsedOccurredAt = occurredAt
            )
            upsertTransactionIntoChartCaches(localTx)
            upsertTransactionIntoAutoInvestCaches(localTx)
            overviewTransactionsCache = overviewTransactionsCache?.let { (listOf(localTx) + it).distinctBy { tx -> tx.id } }
            if (YearMonth.from(occurredAt) == current.selectedYearMonth) {
                val merged = listOf(localTx) + current.transactions
                _uiState.value = _uiState.value.copy(
                    transactions = merged,
                    summary = recomputeSummary(merged)
                )
            }
            syncOverviewFromCacheIfPresent()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                message = "请求失败: ${formatApiError(e)}"
            )
        }
    }

    fun updateTransactionAfterCategory(
        transactionId: Long,
        type: Int,
        categoryName: String,
        iconKey: String,
        amount: Double,
        note: String,
        occurredAt: LocalDateTime = LocalDateTime.now(),
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        try {
            _uiState.value = _uiState.value.copy(loading = true, message = "")
            var categoryId = 0L
            callWithTokenRefreshOnce {
                categoryId = ensureCategoryId(type, categoryName, iconKey)
                val updateRes = api.updateTransaction(
                    authHeader(),
                    transactionId,
                    TransactionUpsertRequest(
                        type = type,
                        amount = amount,
                        categoryId = categoryId,
                        note = note.ifBlank { null },
                        occurredAt = formatDateTime(occurredAt)
                    )
                )
                ensureOk(updateRes)
            }
            _uiState.value = _uiState.value.copy(loading = false, message = "明细已更新")
            onSuccess()
            val current = _uiState.value
            val inCurrentMonth = YearMonth.from(occurredAt) == current.selectedYearMonth
            val edited = TransactionItem(
                id = transactionId,
                type = type,
                amount = amount,
                categoryId = categoryId,
                categoryName = categoryName,
                categoryIcon = iconKey,
                note = note.ifBlank { null },
                occurredAt = formatDateTime(occurredAt),
                parsedOccurredAt = occurredAt
            )
            val merged = if (inCurrentMonth) {
                current.transactions.map { if (it.id == transactionId) edited else it }
            } else {
                current.transactions.filterNot { it.id == transactionId }
            }
            removeTransactionFromChartCaches(transactionId)
            removeTransactionFromAutoInvestCaches(transactionId)
            upsertTransactionIntoChartCaches(edited)
            upsertTransactionIntoAutoInvestCaches(edited)
            overviewTransactionsCache = overviewTransactionsCache?.map { if (it.id == transactionId) edited else it }
            _uiState.value = _uiState.value.copy(
                transactions = merged,
                summary = recomputeSummary(merged)
            )
            syncOverviewFromCacheIfPresent()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                message = "请求失败: ${formatApiError(e)}"
            )
        }
    }

    private suspend fun ensureCategoryId(type: Int, name: String, iconKey: String): Long {
        val listRes = api.getCategories(authHeader(), type)
        ensureOk(listRes)
        val list = listRes.data.orEmpty()
        list.find { it.name == name }?.let { return it.id }
        val createRes = api.createCategory(
            authHeader(),
            CategoryUpsertRequest(type = type, name = name, icon = iconKey, sort = 0)
        )
        ensureOk(createRes)
        val list2Res = api.getCategories(authHeader(), type)
        ensureOk(list2Res)
        val list2 = list2Res.data.orEmpty()
        return list2.find { it.name == name }?.id ?: error("分类创建失败，请重试")
    }

    private suspend fun fetchMonthData(auth: String, ym: YearMonth): Pair<List<TransactionItem>, StatsSummary?> {
        val from = ym.atDay(1).atStartOfDay()
        val to = ym.plusMonths(1).atDay(1).atStartOfDay()
        val fromStr = formatDateTime(from)
        val toStr = formatDateTime(to)
        val tx = api.getTransactions(auth, fromStr, toStr, null, 1, 500).data?.records.orEmpty()
            .map { it.withParsedOccurredAt() }
        val summary = api.getSummary(auth, fromStr, toStr).data
        return tx to summary
    }

    suspend fun fetchTransactionsForRange(from: LocalDateTime, to: LocalDateTime): List<TransactionItem> {
        return callWithTokenRefreshOnce {
            val res = api.getTransactions(
                authHeader(),
                formatDateTime(from),
                formatDateTime(to),
                null,
                1,
                1000
            )
            ensureOk(res)
            res.data?.records.orEmpty().map { it.withParsedOccurredAt() }
        }
    }

    private fun TransactionItem.withParsedOccurredAt(): TransactionItem {
        if (parsedOccurredAt != null) return this
        return copy(parsedOccurredAt = parseOccurredAtToLocalDateTime(occurredAt))
    }

    private fun recomputeSummary(list: List<TransactionItem>): StatsSummary {
        val income = list.filter { it.type == 2 }.sumOf { it.amount }
        val expense = list.filter { it.type == 1 }.sumOf { it.amount }
        return StatsSummary(
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense
        )
    }

    private suspend fun fetchAllTransactionsForOverview(): List<TransactionItem> {
        return callWithTokenRefreshOnce {
            val auth = authHeader()
            val from = LocalDate.of(2000, 1, 1).atStartOfDay()
            val to = LocalDate.now().plusDays(1).atStartOfDay()
            var page = 1L
            val size = 500L
            val all = mutableListOf<TransactionItem>()
            while (true) {
                val res = api.getTransactions(
                    auth,
                    formatDateTime(from),
                    formatDateTime(to),
                    null,
                    page,
                    size
                )
                ensureOk(res)
                val data = res.data ?: break
                val records = data.records.map { it.withParsedOccurredAt() }
                all += records
                if (records.isEmpty() || all.size >= data.total) break
                page++
            }
            all
        }
    }

    private fun computeOverviewStats(list: List<TransactionItem>): Pair<Int, Int> {
        val days = list.mapNotNull { it.parsedOccurredAt?.toLocalDate() }.distinct().size
        return days to list.size
    }

    private fun syncOverviewFromCacheIfPresent() {
        val cache = overviewTransactionsCache ?: return
        val (days, count) = computeOverviewStats(cache)
        _uiState.value = _uiState.value.copy(
            overviewTotalDays = days,
            overviewTotalCount = count
        )
    }

    private fun ensureOk(res: ApiResponse<*>) {
        if (res.code != 0) error(res.message.ifBlank { "服务端返回异常" })
    }

    /** access 过期或无效时 Spring 会返回 401/403，此处用 refresh 换新 token 后重试一次 */
    private suspend fun <T> callWithTokenRefreshOnce(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) {
                try {
                    refreshAccessTokenOrThrow()
                    return block()
                } catch (_: Exception) {
                    forceLogoutOnAuthExpired()
                    throw IllegalStateException("登录已过期，请重新登录")
                }
            }
            throw e
        }
    }

    private suspend fun refreshAccessTokenOrThrow() {
        val rt = sessionStore.getRefreshToken() ?: error("登录已失效，请重新登录")
        val res = api.refresh(TokenRefreshRequest(rt))
        ensureOk(res)
        val data = requireNotNull(res.data) { "刷新 token 失败" }
        sessionStore.saveTokens(data.accessToken, data.refreshToken)
    }

    private fun formatApiError(e: Throwable): String {
        if (e is HttpException) {
            val code = e.code()
            return when (code) {
                401, 403 -> "登录已过期或无效（HTTP $code），已尝试刷新；若仍失败请重新登录"
                else -> e.message ?: "HTTP $code"
            }
        }
        return e.message ?: e.javaClass.simpleName
    }

    fun applyTemplate(templateId: Long) = viewModelScope.launch {
        runAction {
            api.applyTemplate(authHeader(), templateId)
            loadHomeData()
            "模板已应用"
        }
    }

    fun createTemplateFromTransaction(tx: TransactionItem) = viewModelScope.launch {
        runAction {
            api.createTemplate(
                authHeader(),
                TemplateUpsertRequest(
                    type = tx.type,
                    amount = abs(tx.amount),
                    categoryId = tx.categoryId,
                    note = tx.note,
                    sort = 0
                )
            )
            loadHomeData()
            "已添加到模板"
        }
    }

    private suspend fun runAction(block: suspend () -> String) {
        try {
            _uiState.value = _uiState.value.copy(loading = true, message = "")
            val message = block()
            _uiState.value = _uiState.value.copy(loading = false, message = message)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(loading = false, message = "请求失败: ${e.message}")
        }
    }

    private fun authHeader(): String {
        val token = sessionStore.getAccessToken()
        if (token.isNullOrBlank()) {
            forceLogoutOnAuthExpired()
            error("登录已过期，请重新登录")
        }
        return "Bearer $token"
    }

    private fun forceLogoutOnAuthExpired() {
        val theme = _uiState.value.themeMode
        sessionStore.clear()
        clearChartStats()
        clearAutoInvestState()
        overviewTransactionsCache = null
        _uiState.value = AppUiState(
            loggedIn = false,
            message = "登录已过期，请重新登录",
            themeMode = theme
        )
    }

    private fun formatDateTime(v: LocalDateTime): String {
        return v.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
