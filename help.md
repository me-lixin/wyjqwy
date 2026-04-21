统一约定
鉴权：Authorization: Bearer <token>
统一响应：
成功：{ code: 0, message: "ok", data: ... }
失败：code != 0，message 可直接给前端提示
时间：ISO-8601（例如 2026-04-08T10:30:00）
金额：DECIMAL(18,2)，前端传 number，后端按 2 位处理
Auth
POST /api/auth/register
req: { username, password }
res: ApiResponse<Void>
POST /api/auth/login
req: { username, password }
res: { accessToken, refreshToken }
POST /api/auth/refresh
req: { refreshToken }
res: { accessToken, refreshToken }
Category（混合分类：系统+用户）
列表
GET /api/categories?type=1|2
行为：
返回 user_id=0（系统） + user_id=currentUser（用户自建）
排序：用户自建优先，其次 sort desc, id asc
新建（仅用户自建）
POST /api/categories
req: { type, name, icon, sort }
行为：
写入 user_id=currentUser
若与同 type 的系统分类同名，返回业务错误避免重复
修改/删除（仅用户自建）
PUT /api/categories/{id}
DELETE /api/categories/{id}
行为：
仅 entity.user_id == currentUser 允许
系统分类（user_id=0）一律拒绝
建议返回字段新增：isSystem（或直接返回 userId）供前端禁用编辑按钮。

Transaction（核心）
分页查询
GET /api/transactions?from=&to=&type=&categoryId=&keyword=&page=1&size=200
res: PageResponse<TransactionItem>
TransactionItem 建议：
id, type, amount, categoryId, categoryName, categoryIcon, note, occurredAt
新建
POST /api/transactions
req: { type, amount, categoryId, note, occurredAt }
校验：
categoryId 必须存在，且归属为系统分类或当前用户分类
type 与分类 type 要一致
更新/删除
PUT /api/transactions/{id}
DELETE /api/transactions/{id}
权限：交易必须归当前用户
Template（保留）
GET /api/templates?page=&size=&type=
POST /api/templates
PUT /api/templates/{id}
DELETE /api/templates/{id}
POST /api/templates/{id}/apply
规则：
模板归当前用户
apply 生成交易时，分类需可见（系统/用户）
Stats（单用户口径）
GET /api/stats/summary?from=&to=
res: { totalIncome, totalExpense, balance }
GET /api/stats/trend?from=&to=&type=&granularity=day|month
GET /api/stats/by-category?from=&to=&type=&limit=10
Search（可复用 transactions）
前端用 GET /api/transactions 的 keyword/categoryId/type/from/to 组合即可。

Import / Export / Restore（保留能力，走 server）
导出
GET /api/export?from=&to=&format=json
返回用户全量/区间交易+分类（含系统分类定义）
导入
POST /api/import
支持：
预检（dry-run）
冲突策略（skip/overwrite/merge）
恢复任务
POST /api/restore
GET /api/restore/{taskId}
前端（app）改动对齐点
ApiModels.Category 增加 isSystem
分类管理页：
isSystem=true 禁用编辑/删除
记账页：
创建交易前不再假设分类都是用户的
统一错误处理：
code != 0 一律 toast/snackbar message
移除账本、Supabase/WebDAV、Drift(SQLite)相关的 provider / 参数 / 文案/控件