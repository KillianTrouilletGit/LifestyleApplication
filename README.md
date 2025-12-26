# Personal Leveling System (Operator OS)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-MinSDK%2024-3DDC84.svg?style=flat&logo=android&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-0052CC.svg?style=flat&logo=architecture&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)
![AI](https://img.shields.io/badge/AI-Gemini-orange.svg?style=flat&logo=google-gemini&logoColor=white)

## 🚀 Overview

**Personal Leveling System** is a high-performance Android application that gamifies self-improvement, turning daily habits into an immersive RPG experience. Inspired by the "System" from *Solo Leveling*, it wraps complex fitness, nutrition, and scheduling tools in a custom-built, sci-fi **"Operator OS"** interface.

The app has recently undergone a major transformation, moving from a legacy XML-based architecture to a **Modern Android Development (MAD)** stack using Jetpack Compose, offering a more fluid, reactive, and visually stunning user experience.

---

## 🏗️ Technical Evolution & Architecture

The application is built for speed, offline reliability, and intelligent data processing.

### 🏛️ Modern Android Stack
-   **UI Layer**: Fully refactored to **Jetpack Compose**. A single-activity architecture (`MainActivity`) manages navigation across all modules via a centralized Compose-native NavHost.
-   **Architecture Pattern**: Optimized **MVVM** (Model-View-ViewModel) utilizing `StateFlow` and `collectAsState` for reactive UI updates.
-   **AI Intelligence**: Integrated **Google Gemini AI** for real-time food analysis.
-   **Database**: **Room Database** with a repository pattern, handling complex relational data with an offline-first philosophy.

### 💾 Data Persistence & Relations
-   **Relational Integrity**: Uses robust DAO strategies for many-to-many relationships (Programs, Sessions, Exercises).
-   **Auto-Syncing**: Real-time persistence ensures data integrity even during aggressive system-level resource management.

---

## 🤖 AI Food Scanner (Gemini Integration)

The **Nutrition Monitor** now features a cutting-edge AI scanner that identifies dishes and calculates macronutrients from a single photo.

-   **Tiered Model Strategy**: The system intelligently attempts analysis using a fallback sequence to optimize cost and performance:
    1. `gemini-3-flash` (Primary High-Logic)
    2. `gemini-2.5-flash` (Standard Fallback)
    3. `gemini-2.5-flash-lite` (Efficiency Fallback)
-   **Dynamic API Management**: No hardcoded keys. Users configure their Gemini API key directly in-app, stored securely via `SharedPreferences`.
-   **Human-in-the-Loop**: Users can refine AI-detected fiber and calories before logging, ensuring maximum data accuracy.

---

## 🎨 Design System: "Operator OS"

The "Operator OS" aesthetic has been completely rebuilt as a native Compose design system.

-   **Aesthetic**: Tactical, high-contrast, dark-mode interface inspired by sci-fi HUDs and "Solo Leveling" UI.
-   **Thematic Boot Sequence**: A custom **Interactive Splash Screen** replicates a technical boot sequence (`Initializing Core Modules...`, `Neural Sync...`) before revealing the dashboard.
-   **Adaptive Performance HUD**: A centralized `PerformanceCarousel` provides a rolling overview of level progress, mission efficiency scores, and bio-metrics.
-   **Color DNA**:
    *   *Cyber Cyan* (`#00E5FF`): Interactive data points.
    *   *Neon Magenta* (`#FF00FF`): Level milestones and critical paths.
    *   *Matte Void* (`#000000`): Background for absolute contrast.

---

## 📱 Core Modules

1. **⚔️ Mission System**: Real-time task tracking with an integrated "Efficiency Score" that updates dynamically as you clear daily objectives.
2. **🏋️ Training Protocol**: Advanced workout logger with support for custom programs, weight tracking, and technical timers.
3. **📊 Bio-Metrics Monitor**: Integrated tracking for Sleep, Hydration (H2O), and Nutrition.
4. **🧠 Intelligence (Nutrition)**: Automated Balance Index calculation based on protein/carb/fat ratios, powered by the AI scanning engine.

---

## 🛠️ Setup & Installation

1.  **Deploy**: Install the latest `.apk` on your Android device (MinSDK 24).
2.  **AI Activation (Optional)**:
    *   Obtain a Gemini API key from [Google AI Studio](https://aistudio.google.com/).
    *   In the **Nutrition** module, tap **"TAKE PHOTO"**.
    *   Follow the on-screen prompts to input and save your secure API key.
3.  **Start Leveling**: Complete your daily missions to earn XP and increase your system rank.

---

## 👨‍💻 Author

**Killian Trouillet**
*Engineering Student at ISAE-Supaero*

> *Built with discipline. Executed with precision.*
