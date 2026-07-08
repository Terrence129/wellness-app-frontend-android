# Project Presentation: WellnessApp

## 1. Project Overview
**WellnessApp** is a comprehensive mobile application designed to help users track and improve their physical and mental well-being. By combining traditional wellness logging with modern AI-driven insights, the app provides a holistic approach to personal health management.

---

## 2. Core Features

### 🔐 Authentication & Profile
- **Secure Access**: User registration and login functionality.
- **Session Management**: Persistent login using Token management for a seamless user experience.

### 📝 Wellness Logging
Users can log daily metrics across several key areas:
- **Sleep Tracking**, **Mood Monitoring**, **Hydration**, **Physical Activity**, and **Personal Notes**.

### 📊 Dashboard & History
- **Home View**: A quick overview of today's progress.
- **Historical Data**: Browse past logs to identify trends and patterns.

### 🤖 AI-Powered Wellness
- **AI Wellness Advice**: Personalized recommendations based on historical data.
- **Interactive Chatbot**: A dedicated assistant for real-time wellness support.

---

## 3. Project Structure & Architecture

The project follows the **MVVM (Model-View-ViewModel)** architecture pattern, ensuring a clean separation of concerns and making the codebase easier to test and maintain.

### 📂 Directory Structure
- **`ui/`**: Contains the presentation layer (Activities, ViewModels, and Adapters).
  - `auth/`: Login and Registration.
  - `home/` & `history/`: Dashboard and log viewing.
  - `log/`: Logic for adding and editing daily logs.
  - `ai/`: Interactive AI chatbot and advice generation.
- **`data/`**: The data layer handling all information sources.
  - `model/`: Kotlin data classes (POJOs) for API requests and responses.
  - `network/`: Retrofit configuration and `ApiService` interface.
  - `repository/`: Acts as a mediator between the network and the ViewModels.
- **`util/`**: Utility classes for shared logic like date formatting or UI helpers.

---

## 4. How Everything Works: A Technical Deep Dive

### 🔄 Data Flow Example: Adding a Wellness Log
1. **View (Activity)**: The user enters data and clicks "Save".
2. **ViewModel**: Receives input, validates it, and updates state.
3. **Repository**: Mediates between the ViewModel and the Network.
4. **Networking (Retrofit)**: Sends the request to the Spring Boot backend.
5. **State Update**: The View observes a **LiveData** state and updates the UI reactively.

### 🔍 Explaining the "Important Lines" of Code

#### A. Managing UI State (The Sealed Class Pattern)
```kotlin
sealed class AddLogUiState {
    object Idle : AddLogUiState()
    object Loading : AddLogUiState()
    data class Success(val log: WellnessLogResponse) : AddLogUiState()
    data class Error(val message: String) : AddLogUiState()
}
```
*   **Significance**: This ensures the UI is **type-safe**. We explicitly define every possible state (Idle, Loading, Success, Error), making the app's behavior predictable and robust against edge cases.

#### B. Asynchronous Operations (Coroutines)
```kotlin
viewModelScope.launch {
    _state.value = AddLogUiState.Loading
    val response = repository.createWellnessLog(request)
    // ...
}
```
*   **`viewModelScope.launch`**: Starts a background task. It prevents the app from "hanging" while waiting for the server. It's also lifecycle-aware, meaning it stops automatically if the user leaves the screen.
*   **`repository.createWellnessLog`**: A **suspend** function that "pauses" execution without blocking the main thread.

#### C. Automatic Security (Auth Interceptor)
```kotlin
val authenticatedRequest = originalRequest.newBuilder()
    .addHeader("Authorization", "Bearer $token")
    .build()
```
*   **Significance**: This line in `AuthInterceptor` is the backbone of our security. It automatically injects the **JWT Token** into every outgoing request, so developers don't have to worry about authentication headers in individual API calls.

#### D. Entry Point & Session Routing (MainActivity)
```kotlin
val targetActivity = if (TokenManager(this).hasToken()) {
    HomeActivity::class.java
} else {
    LoginActivity::class.java
}
```
*   **Session Management**: This is the first logic that runs. It checks if the user is already logged in by looking for a saved token. This enables a "Seamless Login" experience.

#### E. Local Data Persistence (TokenManager)
```kotlin
sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
```
*   **Persistence**: Uses Android's `SharedPreferences` to securely store the session token on the device's disk, ensuring the user stays logged in even after closing the app.

---

## 5. Why WellnessApp?
Unlike simple trackers, **WellnessApp** interprets data. By leveraging AI, the project moves from "passive tracking" to "active coaching," helping users make informed decisions about their lifestyle.

---

## 6. Future Roadmap
- Integration with Google Fit/Samsung Health.
- Social features for community challenges.
- Detailed graphical analytics for long-term health trends.

---
*Presented by [Your Name]*
*WellnessApp - Your AI Companion for a Better Life.*
