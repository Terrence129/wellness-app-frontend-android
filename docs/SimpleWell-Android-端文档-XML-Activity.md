# SimpleWell Android 端文档（XML + Activity 版本）

## 1. 文档说明

本文件对应仓库：

```text
Terrence129/wellness-app-frontend-android
https://github.com/Terrence129/wellness-app-frontend-android
```

当前 Android 工程真实配置：

```text
rootProject.name = WellnessApp
namespace = com.example.wellnessapp
applicationId = com.example.wellnessapp
app_name = WellnessApp
theme = Theme.WellnessApp
```

说明：产品/项目文档中可以继续使用 SimpleWell 作为业务名称，但 Android 代码包名、Manifest、主题和资源引用应以当前仓库真实配置为准。

技术栈：

```text
Android
Kotlin
XML Layout
Activity
Retrofit
Coroutines
ViewModel
SharedPreferences / DataStore
RecyclerView
```

本仓库负责 SimpleWell 的 Android 前端，包括登录注册、首页、健康记录新增、历史记录、健康记录详情、健康记录编辑、周总结、AI Advice 和 Chatbot。

重要约定：

```text
本项目 Android 端统一使用 XML + Activity。
不使用 Fragment。
不使用 Navigation Graph。
一个主要页面对应一个 Activity 和一个 XML layout。
Android 只调用 Spring Boot Backend。
Android 不直接调用 Python FastAPI AI Service。
```

整体调用关系：

```text
Android Kotlin App
        ↓
Spring Boot Backend
        ↓
Python FastAPI AI Service
```

---

## 2. 成员职责总览

| 成员 | 在 Android 端的职责 |
|---|---|
| 成员 A | 不主要开发 Android 代码，但需要提供稳定的后端 API，并协助成员 D/E/F/G 联调。 |
| 成员 B | 不主要开发 Android 代码，但需要提供 AI Advice 和 Chatbot API，并协助成员 G 联调。 |
| 成员 C | 负责 Android 项目架构、Retrofit、TokenManager、AuthInterceptor、data model、repository、基础跳转工具、通用 UI 状态。 |
| 成员 D | 负责 LoginActivity、RegisterActivity、登录注册表单校验、登录注册接口调用。 |
| 成员 E | 负责 AddWellnessLogActivity、EditWellnessLogActivity、健康记录新增、编辑、删除。 |
| 成员 F | 负责 HomeActivity、HistoryActivity、WellnessLogDetailActivity、历史记录列表和详情展示。 |
| 成员 G | 负责 WeeklySummaryActivity、AiAdviceActivity、ChatbotActivity，以及整体 UI 风格统一。 |

---

## 3. 当前仓库结构与推荐目标结构

### 3.1 当前仓库初始结构

当前仓库还是 Android Studio 新建项目的基础骨架。成员开发时应基于现有包名 `com.example.wellnessapp` 继续扩展，不要另建 `com.example.simplewell` 包。

```text
wellness-app-frontend-android
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
└── app
    ├── build.gradle.kts
    └── src
        ├── main
        │   ├── AndroidManifest.xml
        │   ├── java
        │   │   └── com
        │   │       └── example
        │   │           └── wellnessapp
        │   │               └── MainActivity.kt
        │   └── res
        │       ├── layout
        │       │   └── activity_main.xml
        │       ├── values
        │       │   ├── colors.xml
        │       │   ├── strings.xml
        │       │   └── themes.xml
        │       ├── values-night
        │       ├── drawable
        │       ├── mipmap-*
        │       └── xml
        ├── test
        └── androidTest
```

### 3.2 成员 C 优先创建的基础包

成员 C 先在 `app/src/main/java/com/example/wellnessapp` 下创建公共基础包：

```text
com/example/wellnessapp
├── MainActivity.kt
├── data
│   ├── local
│   ├── model
│   ├── network
│   └── repository
├── util
└── ui
    └── common
```

这些包是 D/E/F/G 后续页面开发的公共基础。页面负责人后续再分别扩展 `ui/auth`、`ui/home`、`ui/log`、`ui/history`、`ui/summary`、`ui/ai`。

### 3.3 推荐最终结构

```text
wellness-app-frontend-android
├── README.md
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── docs
│   ├── screen-flow.md
│   ├── api-usage.md
│   └── test-cases.md
├── app
│   ├── build.gradle.kts
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── example
│           │           └── wellnessapp
│           │               ├── MainActivity.kt
│           │               ├── data
│           │               │   ├── local
│           │               │   │   └── TokenManager.kt
│           │               │   ├── model
│           │               │   │   ├── ApiResponse.kt
│           │               │   │   ├── AuthModels.kt
│           │               │   │   ├── UserModels.kt
│           │               │   │   ├── WellnessModels.kt
│           │               │   │   ├── SummaryModels.kt
│           │               │   │   └── AiModels.kt
│           │               │   ├── network
│           │               │   │   ├── ApiService.kt
│           │               │   │   ├── RetrofitClient.kt
│           │               │   │   └── AuthInterceptor.kt
│           │               │   └── repository
│           │               │       ├── AuthRepository.kt
│           │               │       ├── UserRepository.kt
│           │               │       ├── WellnessRepository.kt
│           │               │       ├── SummaryRepository.kt
│           │               │       └── AiRepository.kt
│           │               ├── ui
│           │               │   ├── auth
│           │               │   │   ├── LoginActivity.kt
│           │               │   │   ├── RegisterActivity.kt
│           │               │   │   ├── LoginViewModel.kt
│           │               │   │   └── RegisterViewModel.kt
│           │               │   ├── home
│           │               │   │   ├── HomeActivity.kt
│           │               │   │   └── HomeViewModel.kt
│           │               │   ├── log
│           │               │   │   ├── AddWellnessLogActivity.kt
│           │               │   │   ├── EditWellnessLogActivity.kt
│           │               │   │   ├── AddWellnessLogViewModel.kt
│           │               │   │   └── EditWellnessLogViewModel.kt
│           │               │   ├── history
│           │               │   │   ├── HistoryActivity.kt
│           │               │   │   ├── HistoryViewModel.kt
│           │               │   │   ├── WellnessLogDetailActivity.kt
│           │               │   │   └── WellnessLogAdapter.kt
│           │               │   ├── summary
│           │               │   │   ├── WeeklySummaryActivity.kt
│           │               │   │   └── WeeklySummaryViewModel.kt
│           │               │   ├── ai
│           │               │   │   ├── AiAdviceActivity.kt
│           │               │   │   ├── AiAdviceViewModel.kt
│           │               │   │   ├── ChatbotActivity.kt
│           │               │   │   ├── ChatbotViewModel.kt
│           │               │   │   └── ChatMessageAdapter.kt
│           │               │   └── common
│           │               │       ├── LoadingView.kt
│           │               │       └── ErrorView.kt
│           │               └── util
│           │                   ├── DateUtils.kt
│           │                   ├── ValidationUtils.kt
│           │                   ├── UiState.kt
│           │                   └── IntentKeys.kt
│           └── res
│               ├── drawable
│               ├── layout
│               │   ├── activity_main.xml
│               │   ├── activity_login.xml
│               │   ├── activity_register.xml
│               │   ├── activity_home.xml
│               │   ├── activity_add_wellness_log.xml
│               │   ├── activity_history.xml
│               │   ├── activity_wellness_log_detail.xml
│               │   ├── activity_edit_wellness_log.xml
│               │   ├── activity_weekly_summary.xml
│               │   ├── activity_ai_advice.xml
│               │   ├── activity_chatbot.xml
│               │   ├── item_wellness_log.xml
│               │   ├── item_chat_user.xml
│               │   └── item_chat_ai.xml
│               ├── values
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── menu
```

---

## 4. Layout 和 Activity 总览

本项目使用：

```text
11 个 Activity layout
3 个 RecyclerView item layout
总共 14 个 XML layout
```

### 4.1 Activity Layout

| 编号 | Activity | XML Layout | 负责人 |
|---|---|---|---|
| 1 | `MainActivity.kt` | `activity_main.xml` | 成员 C |
| 2 | `LoginActivity.kt` | `activity_login.xml` | 成员 D |
| 3 | `RegisterActivity.kt` | `activity_register.xml` | 成员 D |
| 4 | `HomeActivity.kt` | `activity_home.xml` | 成员 F |
| 5 | `AddWellnessLogActivity.kt` | `activity_add_wellness_log.xml` | 成员 E |
| 6 | `HistoryActivity.kt` | `activity_history.xml` | 成员 F |
| 7 | `WellnessLogDetailActivity.kt` | `activity_wellness_log_detail.xml` | 成员 F / 成员 E |
| 8 | `EditWellnessLogActivity.kt` | `activity_edit_wellness_log.xml` | 成员 E |
| 9 | `WeeklySummaryActivity.kt` | `activity_weekly_summary.xml` | 成员 G |
| 10 | `AiAdviceActivity.kt` | `activity_ai_advice.xml` | 成员 G |
| 11 | `ChatbotActivity.kt` | `activity_chatbot.xml` | 成员 G |

### 4.2 Item Layout

| XML Layout | 用途 | 负责人 |
|---|---|---|
| `item_wellness_log.xml` | History RecyclerView 的健康记录 item | 成员 F |
| `item_chat_user.xml` | Chatbot RecyclerView 的用户消息气泡 | 成员 G |
| `item_chat_ai.xml` | Chatbot RecyclerView 的 AI 回复气泡 | 成员 G |

---

## 5. 页面跳转关系

### 5.1 总体跳转关系

```text
MainActivity
   ├── 如果已有 token -> HomeActivity
   └── 如果没有 token -> LoginActivity

LoginActivity
   ├── 登录成功 -> HomeActivity
   └── 点击 Register -> RegisterActivity

RegisterActivity
   └── 注册成功 -> LoginActivity

HomeActivity
   ├── Add Log -> AddWellnessLogActivity
   ├── History -> HistoryActivity
   ├── Weekly Summary -> WeeklySummaryActivity
   ├── AI Advice -> AiAdviceActivity
   ├── Chatbot -> ChatbotActivity
   └── Logout -> LoginActivity

AddWellnessLogActivity
   ├── Submit 成功 -> HistoryActivity
   └── Cancel -> HomeActivity

HistoryActivity
   └── 点击某条记录 -> WellnessLogDetailActivity

WellnessLogDetailActivity
   ├── Edit -> EditWellnessLogActivity
   ├── Delete 成功 -> HistoryActivity
   └── Back -> HistoryActivity

EditWellnessLogActivity
   ├── Update 成功 -> HistoryActivity
   ├── Delete 成功 -> HistoryActivity
   └── Cancel -> WellnessLogDetailActivity

WeeklySummaryActivity
   └── Back -> HomeActivity

AiAdviceActivity
   └── Back -> HomeActivity

ChatbotActivity
   └── Back -> HomeActivity
```

---

### 5.2 页面流程图

```text
[MainActivity]
      |
      | Check token
      v
[LoginActivity] <-------- [RegisterActivity]
      |
      | Login success
      v
[HomeActivity]
      |
      |------------------------------|
      |                              |
      v                              v
[AddWellnessLogActivity]       [HistoryActivity]
      |                              |
      | Submit success               | Click record
      v                              v
[HistoryActivity]          [WellnessLogDetailActivity]
                                  |
                                  | Edit
                                  v
                         [EditWellnessLogActivity]


[HomeActivity]
      |
      |----> [WeeklySummaryActivity]
      |
      |----> [AiAdviceActivity]
      |
      |----> [ChatbotActivity]
      |
      |----> Logout -> [LoginActivity]
```

---

## 6. 每个页面内容与接口

### 6.1 MainActivity

XML：

```text
activity_main.xml
```

负责人：

```text
成员 C
```

页面内容：

```text
App Logo
Loading Text / ProgressBar
```

职责：

```text
检查本地是否有 JWT token
有 token -> HomeActivity
没有 token -> LoginActivity
```

---

### 6.2 LoginActivity

XML：

```text
activity_login.xml
```

负责人：

```text
成员 D
```

页面内容：

```text
App Name / Logo
Email 输入框
Password 输入框
Login 按钮
Go to Register 按钮
错误提示 TextView
Loading ProgressBar
```

调用 API：

```text
POST /api/auth/login
```

跳转：

```text
登录成功 -> HomeActivity
点击 Register -> RegisterActivity
```

---

### 6.3 RegisterActivity

XML：

```text
activity_register.xml
```

负责人：

```text
成员 D
```

页面内容：

```text
Username 输入框
Email 输入框
Password 输入框
Confirm Password 输入框
Register 按钮
Back to Login 按钮
错误提示 TextView
Loading ProgressBar
```

调用 API：

```text
POST /api/auth/register
```

跳转：

```text
注册成功 -> LoginActivity
Back to Login -> LoginActivity
```

---

### 6.4 HomeActivity

XML：

```text
activity_home.xml
```

负责人：

```text
成员 F
```

页面内容：

```text
欢迎语
今日日期
今日健康状态卡片
Add Wellness Log 按钮
History 按钮
Weekly Summary 按钮
AI Advice 按钮
Chatbot 按钮
Logout 按钮
```

调用 API：

```text
GET /api/users/me
GET /api/wellness-logs/date/{today}
```

跳转：

```text
Add Wellness Log -> AddWellnessLogActivity
History -> HistoryActivity
Weekly Summary -> WeeklySummaryActivity
AI Advice -> AiAdviceActivity
Chatbot -> ChatbotActivity
Logout -> LoginActivity
```

---

### 6.5 AddWellnessLogActivity

XML：

```text
activity_add_wellness_log.xml
```

负责人：

```text
成员 E
```

页面内容：

```text
Date 输入或选择
Sleep Hours 输入框
Mood Score 选择器
Water Cups 输入框
Steps 输入框
Exercise Minutes 输入框
Notes 多行输入框
Submit 按钮
Cancel 按钮
错误提示 TextView
Loading ProgressBar
```

调用 API：

```text
POST /api/wellness-logs
```

跳转：

```text
提交成功 -> HistoryActivity
取消 -> HomeActivity
```

---

### 6.6 HistoryActivity

XML：

```text
activity_history.xml
item_wellness_log.xml
```

负责人：

```text
成员 F
```

页面内容：

```text
标题 Wellness History
日期范围筛选
RecyclerView 健康记录列表
空数据提示
Loading ProgressBar
错误提示 TextView
```

调用 API：

```text
GET /api/wellness-logs
```

跳转：

```text
点击某条记录 -> WellnessLogDetailActivity
Back -> HomeActivity
```

传递参数：

```text
logId
logDate
```

---

### 6.7 WellnessLogDetailActivity

XML：

```text
activity_wellness_log_detail.xml
```

负责人：

```text
成员 F / 成员 E
```

页面内容：

```text
日期
睡眠时间
心情分数
饮水杯数
步数
运动分钟数
备注
Edit 按钮
Delete 按钮
Back 按钮
```

调用 API：

```text
GET /api/wellness-logs/date/{logDate}
DELETE /api/wellness-logs/{id}
```

跳转：

```text
Edit -> EditWellnessLogActivity
Delete 成功 -> HistoryActivity
Back -> HistoryActivity
```

---

### 6.8 EditWellnessLogActivity

XML：

```text
activity_edit_wellness_log.xml
```

负责人：

```text
成员 E
```

页面内容：

```text
日期显示
Sleep Hours 输入框
Mood Score 选择器
Water Cups 输入框
Steps 输入框
Exercise Minutes 输入框
Notes 输入框
Update 按钮
Delete 按钮
Cancel 按钮
错误提示 TextView
Loading ProgressBar
```

调用 API：

```text
PUT /api/wellness-logs/{id}
DELETE /api/wellness-logs/{id}
```

跳转：

```text
Update 成功 -> HistoryActivity
Delete 成功 -> HistoryActivity
Cancel -> WellnessLogDetailActivity
```

---

### 6.9 WeeklySummaryActivity

XML：

```text
activity_weekly_summary.xml
```

负责人：

```text
成员 G
```

页面内容：

```text
标题 Weekly Summary
平均睡眠时间
平均心情分数
平均饮水杯数
总步数
总运动分钟数
summary 文本
日期范围选择
Back 按钮
Loading ProgressBar
错误提示 TextView
```

调用 API：

```text
GET /api/wellness-summary/weekly
```

跳转：

```text
Back -> HomeActivity
```

---

### 6.10 AiAdviceActivity

XML：

```text
activity_ai_advice.xml
```

负责人：

```text
成员 G
```

页面内容：

```text
标题 AI Wellness Advice
Generate AI Advice 按钮
Latest Advice 区域
Advice Date
Advice Text
免责声明
Loading ProgressBar
错误提示 TextView
```

调用 API：

```text
POST /api/ai/advice
GET /api/ai/advice/latest
```

调用流程：

```text
Android -> Spring Boot -> Python FastAPI -> Spring Boot -> Android
```

跳转：

```text
Back -> HomeActivity
Generate AI Advice -> 当前页面刷新 adviceText
```

---

### 6.11 ChatbotActivity

XML：

```text
activity_chatbot.xml
item_chat_user.xml
item_chat_ai.xml
```

负责人：

```text
成员 G
```

页面内容：

```text
标题 Wellness Chatbot
RecyclerView 聊天消息列表
EditText 用户输入框
Send 按钮
用户消息气泡
AI 回复气泡
免责声明
Loading ProgressBar
错误提示 TextView
```

调用 API：

```text
POST /api/ai/chat
GET /api/ai/chat/history
```

调用流程：

```text
Android -> Spring Boot -> Python FastAPI -> Spring Boot -> Android
```

跳转：

```text
Back -> HomeActivity
Send -> 当前页面追加用户消息和 AI 回复
```

---

## 7. AndroidManifest 建议

```xml
<application
    android:theme="@style/Theme.WellnessApp"
    android:label="@string/app_name">

    <activity android:name=".ui.ai.ChatbotActivity" />
    <activity android:name=".ui.ai.AiAdviceActivity" />
    <activity android:name=".ui.summary.WeeklySummaryActivity" />
    <activity android:name=".ui.log.EditWellnessLogActivity" />
    <activity android:name=".ui.history.WellnessLogDetailActivity" />
    <activity android:name=".ui.history.HistoryActivity" />
    <activity android:name=".ui.log.AddWellnessLogActivity" />
    <activity android:name=".ui.home.HomeActivity" />
    <activity android:name=".ui.auth.RegisterActivity" />
    <activity android:name=".ui.auth.LoginActivity" />

    <activity
        android:name=".MainActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

</application>
```

---

## 8. Retrofit API Interface

建议文件：

```text
data/network/ApiService.kt
```

示例：

```kotlin
interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<UserResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): ApiResponse<UserResponse>

    @POST("wellness-logs")
    suspend fun createWellnessLog(
        @Body request: WellnessLogRequest
    ): ApiResponse<WellnessLogResponse>

    @GET("wellness-logs")
    suspend fun getWellnessLogs(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): ApiResponse<List<WellnessLogResponse>>

    @GET("wellness-logs/date/{logDate}")
    suspend fun getWellnessLogByDate(
        @Path("logDate") logDate: String
    ): ApiResponse<WellnessLogResponse>

    @PUT("wellness-logs/{id}")
    suspend fun updateWellnessLog(
        @Path("id") id: Long,
        @Body request: WellnessLogUpdateRequest
    ): ApiResponse<WellnessLogResponse>

    @DELETE("wellness-logs/{id}")
    suspend fun deleteWellnessLog(
        @Path("id") id: Long
    ): ApiResponse<Unit?>

    @GET("wellness-summary/weekly")
    suspend fun getWeeklySummary(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): ApiResponse<WeeklySummaryResponse>

    @POST("ai/advice")
    suspend fun generateAiAdvice(
        @Body request: AiAdviceRequest
    ): ApiResponse<AiAdviceResponse>

    @GET("ai/advice/latest")
    suspend fun getLatestAiAdvice(): ApiResponse<AiAdviceResponse>

    @POST("ai/chat")
    suspend fun sendChatMessage(
        @Body request: ChatRequest
    ): ApiResponse<ChatResponse>

    @GET("ai/chat/history")
    suspend fun getChatHistory(
        @Query("limit") limit: Int?
    ): ApiResponse<List<ChatMessageResponse>>
}
```

---

## 9. Kotlin Data Models

### 9.1 ApiResponse

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
```

### 9.2 Auth Models

```kotlin
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)
```

### 9.3 User Models

```kotlin
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String
)
```

### 9.4 Wellness Models

```kotlin
data class WellnessLogRequest(
    val logDate: String,
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?
)

data class WellnessLogUpdateRequest(
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?
)

data class WellnessLogResponse(
    val id: Long,
    val logDate: String,
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?,
    val createdAt: String? = null
)
```

### 9.5 Summary Models

```kotlin
data class WeeklySummaryResponse(
    val averageSleepHours: Double,
    val averageMoodScore: Double,
    val averageWaterCups: Double,
    val totalSteps: Int,
    val totalExerciseMinutes: Int,
    val summary: String
)
```

### 9.6 AI Models

```kotlin
data class AiAdviceRequest(
    val startDate: String,
    val endDate: String
)

data class AiAdviceResponse(
    val adviceDate: String,
    val adviceText: String
)

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val reply: String,
    val createdAt: String?
)

data class ChatMessageResponse(
    val id: Long,
    val sender: String,
    val message: String,
    val createdAt: String
)
```

---

## 10. Base URL 注意事项

Android 模拟器访问本机 Spring Boot：

```text
http://10.0.2.2:8080/api/
```

真机访问电脑 Spring Boot：

```text
http://电脑局域网IP:8080/api/
```

例如：

```text
http://192.168.1.10:8080/api/
```

不要在 Android 中调用：

```text
http://localhost:8000
```

因为 Python AI 服务只给 Spring Boot 内部调用。

---

## 11. 成员具体任务

### 11.1 成员 C

成员 C 先做公共基础，不直接负责 Login / Home / History 等具体业务页面。当前仓库已存在 `MainActivity.kt`、`activity_main.xml`、`AndroidManifest.xml` 和基础 Gradle 配置，因此成员 C 应从“确认构建 + 扩展现有包结构”开始。

```text
0. 确认 Gradle Sync 和 :app:assembleDebug 可以通过
1. 基于 com.example.wellnessapp 创建基础包结构
2. Android Kotlin 项目基础配置维护
3. XML + Activity 基础结构
4. RetrofitClient
5. ApiService
6. AuthInterceptor
7. TokenManager
8. data/model
9. repository
10. UiState
11. DateUtils
12. ValidationUtils
13. IntentKeys
14. activity_main.xml
15. MainActivity token 检查逻辑
```

---

### 11.2 成员 D

```text
1. LoginActivity.kt
2. RegisterActivity.kt
3. LoginViewModel.kt
4. RegisterViewModel.kt
5. activity_login.xml
6. activity_register.xml
7. 登录表单校验
8. 注册表单校验
9. 调用 login API
10. 调用 register API
11. 登录成功保存 token
12. 登录成功跳转 HomeActivity
13. 注册成功跳转 LoginActivity
```

---

### 11.3 成员 E

```text
1. AddWellnessLogActivity.kt
2. EditWellnessLogActivity.kt
3. AddWellnessLogViewModel.kt
4. EditWellnessLogViewModel.kt
5. activity_add_wellness_log.xml
6. activity_edit_wellness_log.xml
7. 健康记录输入校验
8. 创建健康记录
9. 更新健康记录
10. 删除健康记录
11. 与 History / Detail 页面跳转联动
```

---

### 11.4 成员 F

```text
1. HomeActivity.kt
2. HomeViewModel.kt
3. HistoryActivity.kt
4. HistoryViewModel.kt
5. WellnessLogDetailActivity.kt
6. WellnessLogAdapter.kt
7. activity_home.xml
8. activity_history.xml
9. activity_wellness_log_detail.xml
10. item_wellness_log.xml
11. 首页功能入口
12. 历史记录列表
13. 健康记录详情
14. Logout 入口
```

---

### 11.5 成员 G

```text
1. WeeklySummaryActivity.kt
2. WeeklySummaryViewModel.kt
3. AiAdviceActivity.kt
4. AiAdviceViewModel.kt
5. ChatbotActivity.kt
6. ChatbotViewModel.kt
7. ChatMessageAdapter.kt
8. activity_weekly_summary.xml
9. activity_ai_advice.xml
10. activity_chatbot.xml
11. item_chat_user.xml
12. item_chat_ai.xml
13. 周总结展示
14. AI Advice 展示
15. Chatbot 一问一答
16. AI 免责声明
17. 整体 UI 风格统一
```

---

## 12. Android 端注意事项

```text
1. 全项目统一 XML + Activity，不混用 Fragment。
2. 每个主要页面一个 Activity。
3. 所有 API 都通过 ApiService 定义。
4. 页面不要直接创建 Retrofit，应通过 Repository 调用。
5. JWT token 统一由 TokenManager 管理。
6. 受保护接口自动添加 Authorization header。
7. 需要统一 Loading、Success、Error、Empty 状态。
8. Chatbot 是必做页面。
9. AI Advice 是必做页面。
10. Android 不直接调用 Python AI 服务。
```


