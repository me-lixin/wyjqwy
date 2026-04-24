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

请作为 Android 开发专家，使用 Jetpack Compose 帮我开发一个的「定投」页面，

具体需求如下：
1. 顶部导航栏 (TopBar)：标题为「定投管理」，右侧不保留设置（Settings）和添加（Add）
2. 上半部分（概览卡片）：
   - 在一个带圆角和阴影的白色 Surface/Card 中实现。
   - 顶部展示核心概览数据，参考图中的总资产、总次数、总时长（总时长以天为单位，统计从第一次分类为投资的支出算起。
   - 中间直接复用或手写一个环形饼图（Canvas 实现），展示「投资分类构成」，在投资分类中使用备注来区分不同的投资种类，不区分大小写，环形中间显示总金额。
   - 饼图下方需要有对应的图例（点状色块 + 分类名称 + 百分比）。
3. 下半部分（列表区域）：
   - 直接复用我现有的分类列表逻辑，只是分类逻辑改为使用备注来分类
   - 列表请使用 LazyColumn 构建，保证滑动的丝滑体验。

你现在是一个资深的 Android Kotlin 架构师兼 UI/UX 设计师。我需要为当前应用设计一套极具美感的“个性化装扮”页面背景纹理系统。
核心要求是：**完全不使用外部外部图片（PNG/JPG/SVG），而是通过 Android 的 XML Drawable 代码、LayerDrawable 或 Shader 来在代码层面生成纹理感**，以保证高性能和适配性。

请按照以下分阶段指令执行，**请先给我第一阶段的代码，并等待我的确认**：

---

### 第一阶段：纹理 Drawable 设计系统定义 (res/drawable/)
1. **建立全局色彩系统 (Color System)**：在 `res/values/colors.xml` 中定义以下主题色的主色和副色（例如：`forest_start` 和 `forest_end`），色值应参考截图（`image_3.png` 到 `image_8.png`）的基色。
2. **设计“渐变流光 (Gradient Flow)”纹理**:
   - 创建 `bg_texture_gradient.xml` (shape drawable)。
   - 使用 `<gradient>` 属性。主色作为 `startColor`，副色作为 `endColor`。
   - 角度必须可以配置（例如，通过自定义属性或代码在运行时 `apply`）。
3. **设计“几何图案 (Pattern Overlay)”纹理**:
   - 创建 `bg_texture_pattern.xml` (layer-list drawable)。
   - **底层**：`<shape>` 填充主色。
   - **顶层**：创建一个简单的几何图案 XML（波点、网格、斜线），并通过 `<item android:alpha="0.1">` 属性将其不透明度设为 10%，形成淡色层叠纹理。
4. **设计“噪点拉丝 (Noise Texture)”纹理**:
   - 这是一个高级 Shader 效果。创建一个简单的 `NoiseDrawable.kt`，它继承自 `Drawable`。
   - 在其 `onDraw` 方法中，使用 `Canvas.drawRect` 绘制一个噪点纹理或 Perlin 噪点纹理（通过简单的随机数生成即可模拟出高级质感）。
   - 图形应随主色进行 tint。
5. **建立排版系统（Typography System）**：配置全局 `textTheme`，严格规范字号与字重：
   - 导航头部 (AppBar title): `titleLarge` -> `fontSize: 18, fontWeight: w600`
   - 页面/模块大标题: `titleMedium` -> `fontSize: 16, fontWeight: w600`
   - 核心数据/金额数字 (如 +200.00): `displaySmall` -> `fontSize: 24, fontWeight: w700` (如果项目中有数字字体如 DIN，请备注)
   - 列表主文本/常真正文: `bodyLarge` -> `fontSize: 15, fontWeight: w400`
   - 辅助说明/次要文本/时间: `bodyMedium` -> `fontSize: 13, fontWeight: w400, color: 浅灰色`
   - 分类标签/微小字: `labelSmall` -> `fontSize: 11, fontWeight: w400`
---

### 第二阶段：背景切换逻辑与代码实现 (Kotlin)
1. 创建 `ThemeBackgroundManager.kt` 类。
2. 定义一个枚举 `TextureType`：`GRADIENT_FLOW`, `PATTERN_OVERLAY`, `NOISE_TEXTURE`。
3. 提供一个核心方法：`fun applyTextureToView(view: View, type: TextureType, primaryColor: Int)`。
4. 在该方法中，根据类型和主色，在运行时创建或获取对应的 Drawable，并将其设置为 View 的背景。例如，对于渐变，可以动态创建一个 `GradientDrawable` 并应用 `primaryColor`。

---

### 第三阶段：“个性化”装扮页面 UI 重构 (Fragment/Activity)
1. 编写一个 Fragment (使用 Compose 或 XML 布局)。顶部 AppBar 遵循统一规范。
2. 主体是一个网格布局 (RV 或 Compose Grid, 2列，间距标准)。
3. 每个主题卡片样式：
   - 统一圆角 `12dp`。
   - 卡片本身是一个 `FrameLayout`。底层是该主题的 `primaryColor`。
   - 顶层使用第一阶段设计的某个纹理 Drawable 作为前景或覆盖层（不透明度在 10% 左右）。
4. 确保在切换主题时，不仅切换卡片，也要调用 `ThemeBackgroundManager` 来改变整个 Fragment 的页面背景纹理。

---

### 第四阶段：统一样式抽象与细节处理 (res/values/)
1. 创建 `styles_print.xml` 或对应的 Compose Token 主题。
2. 封装无法由代码直接设置的纹理细节策略：
   - 全局卡片弥散阴影 (Shadow Style)
   - 快速获取纹理装饰的方法（例如，在代码中定义一个方法，快速将任意图标染上主色并加上淡色纹理背景装饰）。

---

### 第五阶段：全局代码重构与替换准备 (指引)
完成以上基础建设后，我将配合你扫描现有的业务页面。请你指导我或自动将以下写死的颜色代码进行替换：
- **颜色替换**：所有的 `Colors.xxx` 替换为 `ThemeUtils.getPrimaryColor(context)`。
- **纹理替换**：把写死的纯色背景区域替换为符合我们新系统的纹理背景。

现在，**请先执行第一阶段**，给我 `colors.xml` 和 `bg_texture_gradient.xml` 的完整代码。
