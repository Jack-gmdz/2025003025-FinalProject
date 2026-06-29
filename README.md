# 课程与作业管理

GitHub 仓库地址：https://github.com/Jack-gmdz/2025003025-FinalProject

## 1. 项目简介

- 应用名称：课程与作业管理 (Course & Assignment Manager)
- 目标用户：需要管理大学课程和作业的学生
- 核心功能：
  - 课程信息的添加、编辑、删除和搜索
  - 作业的添加、状态流转（待提交→已提交→已批阅）、逾期检测
  - 基于 Mock API 的课程建议搜索和学期信息展示
  - 用户偏好设置持久化（昵称、提醒天数等）
  - 深浅色主题自动切换

## 2. 技术栈

- UI：Jetpack Compose + Material 3
- 数据库：Room
- 网络：Retrofit 2.9.0 + OkHttp 4.12.0（Mock 数据来源于自定义 OkHttp Interceptor）
- 状态管理：ViewModel + StateFlow
- 持久化偏好：DataStore Preferences
- 导航：Navigation Compose 2.8.0
- 异步处理：Kotlin Coroutines 1.7.3
- 其他依赖：Gson 2.10.1（JSON 解析）、KSP（Room 编译期注解处理）

## 3. 功能清单

### 必做项完成情况

**UI 层**
- [x] Jetpack Compose 构建全部 UI
- [x] 至少 2 个主要页面（首页、课程列表、作业管理、设置共 4 页）
- [x] Compose Navigation 导航
- [x] LazyColumn 列表（课程列表、作业列表均使用 LazyColumn）
- [x] Material 3 组件和主题（Card、FAB、TabRow、ExposedDropdownMenu、AlertDialog、FilterChip 等）
- [x] 浅色 / 深色模式支持（自动跟随系统，通过 `isSystemInDarkTheme()` 切换）

**数据层**
- [x] Room 数据库，至少 2 张表（courses + assignments，通过外键关联）
- [x] 完整 CRUD 操作（两表均支持 Insert / Update / Delete / Query）
- [x] DAO 查询方法返回 Flow 类型（getAllCourses、getAllAssignments 等返回 Flow）
- [x] 至少一种查询功能（课程名/教师模糊搜索、按学期/状态筛选、逾期检测）
- [x] DataStore 保存用户偏好（昵称、显示模式、提醒天数、最近搜索记录）

**网络层**
- [x] 声明并使用 Internet 权限
- [x] 使用网络请求获取 Mock API 数据（OkHttp MockInterceptor 拦截请求并返回预设 JSON）
- [x] 网络数据在核心页面中展示（首页展示学期信息，课程搜索展示课程建议）
- [x] 处理 Loading / Success / Error 等网络状态（sealed interface UiState 包含 Idle、Loading、Success、Error）
- [x] Composable 不直接发起网络请求（通过 Repository → ViewModel → UiState 单向数据流）

**架构层**
- [x] ViewModel 状态管理（CourseViewModel + AssignmentViewModel）
- [x] Repository 模式（CourseRepository 封装 DAO + NetworkDataSource）
- [x] StateFlow / Flow 数据流（ViewModel 持有 MutableStateFlow，暴露 StateFlow）
- [x] Kotlin 协程异步处理（viewModelScope.launch 调用 suspend 函数）
- [x] UiState 描述界面状态
- [x] Composable 不直接访问数据库或网络

**功能完整性**
- [x] 新增 / 编辑 / 删除 / 搜索等核心操作
- [x] 输入验证和错误提示（课程名/作业名非空校验，红色错误提示文字）
- [x] 状态展示（空列表占位提示、加载中动画、逾期提醒卡片）
- [x] 屏幕旋转后状态保持（ViewModel 存活于 Activity 重建期间）

### 选做项完成情况

- [x] 复杂数据库查询：逾期作业查询（WHERE dueDate < now AND status = 'pending'）、多条件筛选
- [x] 搜索防抖：通过 StateFlow 收集搜索输入，网络建议仅在输入 >= 2 字符时触发
- [x] 搜索历史：DataStore 保存最近搜索记录，保留最近 10 条
- [x] 外键关联：assignments 表的 courseId 关联 courses 表的 id，级联删除
- [x] 下拉预设选项：课程添加对话框支持预设选项 + 自定义输入，免去中文输入法切换
- [x] 作业状态流转：pending → submitted → graded，一键切换

## 4. 数据库设计

### 表 1：courses（课程表）

| 字段名    | 类型   | 说明                         |
|-----------|--------|------------------------------|
| id        | Long   | 主键，自增                   |
| name      | String | 课程名称                     |
| teacher   | String | 授课教师                     |
| classroom | String | 上课教室                     |
| schedule  | String | 上课时间（如"周一 8:00-9:40"）|
| credits   | Int    | 学分                         |
| semester  | String | 学期（如"2025-2026-2"）      |

### 表 2：assignments（作业表）

| 字段名      | 类型   | 说明                                                |
|-------------|--------|-----------------------------------------------------|
| id          | Long   | 主键，自增                                          |
| courseId    | Long   | 外键，关联 courses.id，级联删除                     |
| title       | String | 作业标题                                            |
| description | String | 作业描述                                            |
| dueDate     | Long   | 截止日期（epoch 毫秒时间戳）                         |
| status      | String | 状态：pending（待提交）/ submitted（已提交）/ graded（已批阅）|
| priority    | Int    | 优先级：0 = 普通，1 = 重要，2 = 紧急                 |
| grade       | String | 成绩                                                |
| feedback    | String | 教师反馈                                            |

**表关系**：assignments 表通过 `courseId` 外键关联 courses 表，设置 `ON DELETE CASCADE` 使得删除课程时自动删除关联作业。在 `courseId` 列上建立索引以优化查询性能。

**主要 DAO 查询方法**：
- `getAllCourses(): Flow<List<CourseEntity>>` — 获取全部课程，返回 Flow 实现响应式更新
- `searchCourses(query): List<CourseEntity>` — 按课程名或教师模糊搜索
- `getAssignmentsByStatus(status): Flow<List<AssignmentEntity>>` — 按状态筛选作业
- `getOverdueAssignments(now): List<AssignmentEntity>` — 查询逾期且未提交的作业

## 5. 网络功能设计

- API 来源：自定义 Mock API（通过 OkHttp Interceptor 拦截所有请求并返回本地构造的 JSON）
- 接口地址：
  - `GET /api/courses/suggestions?q=keyword` — 课程建议搜索
  - `GET /api/semester/info` — 学期信息
  - `GET /api/courses` — 课程列表
- 请求方式：GET
- 主要返回字段：
  - 课程建议：id, name, teacher, credits, description
  - 学期信息：semester, startDate, endDate, weekCount, currentWeek
- App 中使用这些网络数据的页面或功能：
  - 首页展示当前学期信息（学期名称、当前周数、总周数）
  - 课程列表页搜索框输入时展示来自网络的课程建议
- 网络失败时的处理方式：`runCatching` 包装网络调用，通过 `Result.onFailure` 静默处理错误，UI 通过 UiState.Error 展示错误状态

## 6. 架构设计

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                       │
│  HomeScreen / CourseListScreen /                 │
│  AssignmentListScreen / SettingsScreen            │
│  (Composable 函数，仅消费 StateFlow)              │
└──────────────────┬──────────────────────────────┘
                   │ 持有 StateFlow<UiState>
                   │
┌──────────────────▼──────────────────────────────┐
│                 ViewModel                         │
│  CourseViewModel / AssignmentViewModel            │
│  (持有 MutableStateFlow，暴露 StateFlow)          │
│  (通过 viewModelScope.launch 调用 Repository)     │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│              Repository                           │
│  CourseRepository                                │
│  (整合 CourseDao + AssignmentDao                  │
│   + DataStore + NetworkDataSource)               │
└────┬──────────┬───────────┬─────────────────────┘
     │          │           │
┌────▼──┐ ┌────▼──┐  ┌─────▼──────┐
│ Room   │ │DataSt.│  │  Network    │
│ DAO    │ │       │  │  (Mock)     │
└────────┘ └───────┘  └────────────┘
```

数据流向：**UI Layer** 通过 `collectAsState()` 订阅 ViewModel 的 `StateFlow`。ViewModel 通过 `viewModelScope.launch` 调用 Repository 的 suspend 函数。Repository 作为唯一数据源，整合 Room DAO（本地持久化）、DataStore（用户偏好）和 NetworkDataSource（远程/Mock 数据）。Composable 不直接调用数据库或网络 API，所有数据操作都经过 ViewModel 和 Repository。

## 7. 核心功能截图

### 首页
![首页截图](screenshots/home.png)
说明：展示当前学期信息卡片、课程数量与逾期作业统计快捷入口、逾期提醒红色警告卡片、最近课程列表。用户可通过顶部导航进入设置页。
![alt text](Screenshot_20260629_102625.png)
### 课程列表
![课程列表页截图](screenshots/course_list.png)
说明：展示全部课程，支持搜索框输入触发网络课程建议，支持学期筛选 Tab。每门课程显示名称、教师、时间、教室、学期标签。右侧编辑/删除按钮。FAB 添加新课程。添加/编辑对话框采用下拉预设选项（16 门计算机相关课程 + 20 位百家姓教师 + 10 间教室 + 11 个时间段 + 6 档学分 + 4 个学期），每个下拉最后有"自定义..."选项可手动输入。
![alt text](Screenshot_20260629_102810.png)![c:\Users\ASUS\OneDrive\桌面\lab\Screenshot_20260629_102913.png](Screenshot_20260629_102837.png)
### 作业管理
![作业管理页截图](screenshots/assignment_list.png)
说明：按"全部/待提交/已提交/已批阅"Tab 筛选，作业卡片展示标题、关联课程、截止日期、优先级色标、状态标签。一键切换作业状态（待提交→已提交→已批阅）。已批阅作业显示成绩和反馈。逾期未提交作业红色高亮显示。
![alt text](Screenshot_20260629_102945.png)![alt text](Screenshot_20260629_103000.png)![alt text](Screenshot_20260629_103156.png)
### 设置
![设置页截图](screenshots/settings.png)
说明：昵称编辑与保存，截止日提醒天数（1-14 天滑块调节），关于信息。

## 8. 技术难点与解决方案

### 难点 1：下拉选择 + 自定义输入的组合控件

- 问题描述：课程添加对话框中，用户可能没有中文输入法，需要既能从预设选项中快速选择，又能在需要时手动输入任意内容。
- 原因分析：传统的 `ExposedDropdownMenuBox` 只支持从列表选择，不支持切换到自由输入模式。
- 解决方案：封装 `DropdownWithCustom` 组件，使用 `isCustom` 状态变量控制模式切换。预设模式下显示下拉菜单，菜单末尾增加"自定义..."选项；选择自定义后使用 `AnimatedVisibility` 展开文本输入框，输入内容实时同步到父组件状态。
- 参考资料：Material 3 ExposedDropdownMenuBox 官方文档

### 难点 2：Mock 数据的统一拦截方案

- 问题描述：项目需要网络功能但无真实后端 API，需要模拟多个接口的不同返回数据。
- 原因分析：直接在代码中 hardcode 假数据无法满足"使用网络请求"的要求，需要模拟真实的 HTTP 请求-响应流程。
- 解决方案：使用 OkHttp 的 `Interceptor` 机制，创建 `MockInterceptor` 拦截所有请求，根据 URL path 匹配不同接口，返回对应的 JSON 字符串。Retrofit 和 Gson 正常工作，对上层代码完全透明。

### 难点 3：源文件意外丢失后的快速恢复

- 问题描述：清理命令中路径回溯错误导致 `app/src/main` 整个目录被误删，所有源文件丢失。
- 原因分析：`rd /s /q` 命令中 `..` 回溯层级计算失误，导致删除了 `app/src/main` 而非目标子目录。
- 解决方案：根据之前生成的完整项目记录，逐一重建所有 23 个源文件（Entity、DAO、Database、Network、Repository、ViewModel、UI、Navigation、Manifest），恢复后零编译错误。

## 9. AI 使用说明

请在以下选项中勾选，可多选：

- [ ] 未使用 AI
- [√] 网页版 AI（如 ChatGPT、Claude、Kimi、豆包等）
- [ ] AI Agent / 编程代理（如 Claude Code、Codex、OpenCode、Cursor Agent 等）
- [√] 国产大模型服务（如 DeepSeek、GLM、通义千问、文心一言等）
- [√] IDE 插件或代码补全工具（如 GitHub Copilot、Cursor、CodeGeeX 等）
- [ ] 其他：

具体工具名称：
豆包、通义千问、Codebuddy
AI 主要用于哪些环节：（如选题分析、代码生成、调试、报告整理等）
代码逻辑梳理、代码报错调试、实验报告文字润色、知识点概念解释、代码示例参考编写
说明：是否使用 AI 以及使用了什么 AI 工具不会影响分值，请如实填写。

## 10. 运行说明

- 最低 Android 版本：API 24（Android 7.0）
- 推荐 Android 版本：API 35（Android 15）/ 目标 SDK 36
- 特殊权限：`android.permission.INTERNET`（网络权限，用于网络请求框架运行）
- 运行步骤：
  1. 克隆仓库：`git clone https://github.com/Jack-gmdz/2025003025-FinalProject
  2. 使用 Android Studio（Hedgehog 或更新版本）打开项目
  3. 等待 Gradle 同步完成（需 KSP 插件 2.1.0-1.0.29 编译 Room）
  4. 连接 Android 模拟器（API 24+）或真机，点击 Run

## 11. 项目亮点（可选）

1. **无中文输入法友好设计**：课程添加/编辑对话框所有字段均提供预设下拉选项（16 门课程、20 位教师、10 间教室、11 个时间段等），末尾"自定义..."可展开输入框，完全脱离中文输入法操作。
2. **完整的作业生命周期管理**：作业状态从"待提交"→"已提交"→"已批阅"一键流转，支持批阅时录入成绩和反馈，逾期自动检测并红色高亮提醒。
3. **Mock 网络层透明设计**：通过 OkHttp Interceptor 模拟完整 HTTP 请求-响应流程，Retrofit + Gson 正常工作，切换到真实 API 只需替换 baseUrl 和移除 Interceptor。
4. **多维度筛选与搜索**：作业按状态 Tab 筛选，课程按学期筛选和模糊搜索，网络搜索建议在本地搜索之外的补充。

## 12. 未来改进方向（可选）

1. **日历视图**：增加日历页面，以月视图/周视图直观展示课程时间表和作业截止日分布。
2. **通知提醒**：基于 DataStore 中的提醒天数设置，通过 WorkManager 定时检查即将到期的作业并发送系统通知。
3. **数据统计分析**：增加作业完成率、各科成绩趋势、逾期频率等统计图表。
4. **数据导入导出**：支持课程/作业数据的 JSON 导入导出，便于备份或迁移。
5. **真实后端对接**：将 Mock API 替换为 Firebase Realtime Database 或自建 Spring Boot 后端。
