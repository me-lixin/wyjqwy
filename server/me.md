# Server 已实现功能清单

## 1. 技术栈与项目结构

- Spring Boot 3（Maven, JDK17）
- MyBatis-Plus（注解 / Wrapper 风格）
- Spring Security + JWT（access / refresh）
- Flyway（数据库初始化脚本）
- MySQL 驱动已接入

主要目录：

- `src/main/java/com/wyjqwy/server/controller`
- `src/main/java/com/wyjqwy/server/service`
- `src/main/java/com/wyjqwy/server/mapper`
- `src/main/java/com/wyjqwy/server/model`
- `src/main/resources/db/migration`

## 2. 数据库（已实现）

Flyway 脚本：`src/main/resources/db/migration/V1__init_schema.sql`

已创建表：

- `app_user`
- `category`
- `book_transaction`
- `template`

当前数据库策略（按你的要求）：

- ID：`BIGINT AUTO_INCREMENT`
- 金额：`DECIMAL(18,2)`
- 无外键
- 无索引
- 物理删除（无逻辑删除字段）

## 3. 认证与登录（已实现）

### 接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

### 行为

- 用户名 + 密码注册与登录
- 密码使用 BCrypt 加密
- 登录返回 `accessToken` + `refreshToken`
- 受保护接口需要 `Authorization: Bearer <accessToken>`

## 4. 分类模块（已实现 CRUD）

### 接口

- `GET /api/categories?type=1|2`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### 说明

- 按当前登录用户隔离数据（user_id）
- 删除为物理删除

## 5. 记账模块（已实现 CRUD）

### 接口

- `GET /api/transactions?type=&from=&to=&page=&size=`
- `GET /api/transactions/{id}`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

### 说明

- 支持按类型、时间范围查询
- 时间字段使用 `occurred_at`
- 删除为物理删除
- 如果分类被删除，返回结果中分类名称兜底为 `已删除分类`

## 6. 快捷模板模块（已实现 CRUD）

### 接口

- `GET /api/templates?type=&page=&size=`
- `GET /api/templates/{id}`
- `POST /api/templates`
- `PUT /api/templates/{id}`
- `DELETE /api/templates/{id}`
- `POST /api/templates/{id}/apply`

### 说明

- 按当前登录用户隔离数据
- 如果模板引用分类被删除，返回 `已删除分类`

## 7. 统计模块（已实现）

### 接口

- `GET /api/stats/summary?from=&to=`
- `GET /api/stats/trend?from=&to=&type=&granularity=day|month`
- `GET /api/stats/by-category?from=&to=&type=&limit=10`

### 统计能力

- 汇总：收入、支出、结余
- 趋势：按天/按月聚合
- 分类统计：按分类金额聚合并排序（TOP N）

### 周口径约定

- 业务口径：周一到周日
- 当前实现通过 `from/to` 时间范围控制口径（推荐前端按周一~周日传参）

## 8. 全局能力（已实现）

- 统一返回结构：`ApiResponse(code, message, data)`
- 全局异常处理：参数校验异常、业务异常
- 鉴权后可从上下文获取当前用户 ID

## 9. 测试（已补充核心单元测试）

- `AuthServiceTest`：注册重复用户名、登录成功返回 token、refresh token 类型校验
- `TransactionServiceTest`：分类不存在时兜底“已删除分类”、分类存在时返回真实分类
- `TemplateServiceTest`：分类兜底、模板一键应用生成账单

本地执行：`mvn test`

## 10. 当前未完成 / 可继续项

- 接口文档（Swagger/OpenAPI）未接入
- 集成测试（含数据库/接口级）尚未补充
- Android `app` 目录当前仅创建，尚未开始写移动端代码

