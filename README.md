# Personal Leveling System (Operator OS)

![Language](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg?style=flat&logo=kotlin)
![Platform](https://img.shields.io/badge/Android-MinSDK%2024-green.svg?style=flat&logo=android)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue.svg?style=flat)

**Personal Leveling System** is a gamified self-improvement application for Android, designed to treat personal development like an RPG. Users complete "missions" (daily habits), track "stats" (fitness, health), and earn XP to level up their profile.

This project features a custom "Operator OS" design language, characterized by a dark, tactical UI with high-contrast cyan accents (`#00E5FF`) and technical HUD elements.

> **Note:** This is a **Student Project** developed to demonstrate Android development skills including Room Database, Background Services, and Custom UI implementation.

---

## 📱 Features

### ⚔️ Gamification
-   **XP & Leveling System**: Earn experience points for every completed activity.
-   **Missions**: Daily and Weekly objective tracking with automated resets.
-   **Active Buffs**: Visual indicators of current streaks or achievements.

### 🏋️ Training Module
-   **Program Management**: Create and manage custom workout programs.
-   **Session Tracking**: Log sets, reps, and weights during active sessions.
-   **Specialized Protocols**: Dedicated timers and loggers for **Endurance** (running/cardio) and **Flexibility** training.

### 🥗 Health & Vitality
-   **Nutrition**: Calorie and macro tracking with daily targets.
-   **Hydration**: Water intake logger with visual progress.
-   **Sleep**: Sleep duration monitoring and quality assessment.

### 📊 Data & Analysis
-   **Dashboard**: Centralized grid view of all vital metrics.
-   **Charts**: Visual progress tracking over time (MPAndroidChart).
-   **Local Storage**: Completely offline-first architecture using Room Database.

---

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **UI**: Android View System (XML) with Custom Drawables & Styles
*   **Database**: Room (SQLite)
*   **Background Tasks**: WorkManager & BroadcastReceivers (for mission resets and notifications)
*   **Async**: Coroutines & Flow

---

## 📸 Screenshots

| Dashboard | Training | Profile |
|:---:|:---:|:---:|
| placeholder | placeholder | placeholder |

---

## 🚀 Setup & Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/PersonalLevelingSystem.git
    ```
2.  **Open in Android Studio**:
    *   File -> Open -> Select the cloned directory.
3.  **Sync Gradle**:
    *   Allow Android Studio to download dependencies.
4.  **Run**:
    *   Connect a physical Android device or use an Emulator.
    *   Run the `app` configuration.

---

## 📝 Project Context

This application was built as a capstone/portfolio project to explore:
*   Complex local data relationships (Room).
*   State management in MVVM.
*   Custom UI theming and styling without relying on Material Design defaults.
*   Background process management in modern Android.

---

**Developed by Killian Trouillet**
