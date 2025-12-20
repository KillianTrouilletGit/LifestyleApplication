# Personal Leveling System (Operator OS)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-MinSDK%2024-3DDC84.svg?style=flat&logo=android&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-0052CC.svg?style=flat&logo=architecture&logoColor=white)
![Database](https://img.shields.io/badge/Database-Room-4285F4.svg?style=flat&logo=sqlite&logoColor=white)

## 🚀 Overview

**Personal Leveling System** is a robust Android application that gamifies self-improvement, turning daily habits into an immersive RPG experience. Inspired by the "System" from *Solo Leveling*, it wraps complex fitness, nutrition, and scheduling tools in a custom-built, sci-fi "Operator OS" interface.

This project demonstrates advanced Android development skills, featuring an **offline-first architecture**, **complex relational database modeling**, and a **custom design system** implemented without reliance on standard Material Design libraries.

---

## 🏗️ Technical Architecture

The application is built using modern Android development practices, focusing on scalability and maintainability.

### 🏛️ Architecture Pattern: MVVM (Model-View-ViewModel)
-   **View Layer**: XML-based layouts with custom Drawables and Styles to achieve the unique "Protocol" aesthetic.
-   **ViewModel Layer**: Manages UI state and business logic using `LiveData`. It survives configuration changes and ensures data persistence across the lifecycle.
-   **Model Layer**: A repository pattern abstracting the Room Database data source, ensuring a clean separation of concerns.

### 💾 Data Persistence: Room Database
-   **Complex Relations**: Implements one-to-many and many-to-many relationships (e.g., `Programs` -> `Sessions` -> `Exercises`).
-   **Type Converters**: Custom converters for storing complex objects and timestamps.
-   **Offline First**: All user data is stored locally, ensuring instant access and zero latency.

### ⚡ Concurrency & Background Processing
-   **Coroutines & Flow**: Used for database operations and asynchronous tasks to keep the UI thread unblocked.
-   **BroadcastReceivers**: Handles scheduled events for daily and weekly mission resets (e.g., `DailyMissionResetReceiver`).

---

## 🎨 Design System: "Protocol"

Unlike typical apps that use standard Material Design components, this project features a bespoke design language named **"Protocol"**.

-   **Aesthetic**: Tactical, high-contrast, dark-mode interface inspired by sci-fi HUDs.
-   **Color Palette**:
    -   *Eigengrau* (`#1A1A1A`): Deep matte charcoal background for reduced eye strain.
    -   *Protocol Cyan* (`#00E5FF`): Standardized accent for interactive elements and data visualization.
    -   *Deep Navy* (`#002233`): High-contrast text color for active buttons.
-   **Custom Components**:
    -   **Adaptive Icons**: Custom generated vector assets for all densities (`mdpi` to `xxxhdpi`).
    -   **Technical Shapes**: Custom XML drawables for "cut-corner" cards, bordered inputs, and segmented progress bars.
    -   **Typography**: Monospaced fonts for data values to enhance the technical feel.

---

## 📱 Core Modules

### 1. ⚔️ Gamification Engine
The core loop that drives engagement.
-   **XP Calculation**: Dynamic XP curves based on activity difficulty.
-   **Leveling**: User stats (Strength, Vitality, Intelligence) level up based on specific activity types.

### 2. 🏋️ Training Protocol
A comprehensive workout logger.
-   **Dynamic Session Creation**: Users can build custom programs with variable sets and rep ranges.
-   **Smart Autofill**: The system remembers weights from previous sessions to streamline data entry.
-   **Specialized Timers**: Custom-built chronometers for **Endurance** and **Flexibility** training that handle state preservation across configuration changes.

### 3. 🛡️ Vitality & Logistics
-   **Nutrition**: Macro-nutrient tracking with daily automated resets.
-   **Planning**: Integrated calendar view for scheduling upcoming "Missions".
-   **Sleep & Hydration**: Specialized input interfaces (e.g., custom Time Pickers) for rapid logging.

---

## 💡 Key Technical Challenges Solved

-   **State Preservation**: Solved complex issues with `Cronometer` widgets losing time during activity rotation or backgrounding by implementing robust `onSaveInstanceState` logic and manual time calculation.
-   **Relational Data Integrity**: Designed a cascading deletion strategy in Room to ensure that deleting a "Program" correctly wipes all associated "Sessions" and "Exercises" to prevent orphan data.
-   **Custom Asset Pipeline**: Created a PowerShell automation script (`update_icons.ps1`) to programmatically resize and distribute app icons across all Android density folders, ensuring pixel-perfect assets on every device.

---

## 🛠️ Setup & Installation

-  Download the release 1.0.0 and install it on your android device

---

## 👨‍💻 Author

**Killian Trouillet**
*Engineering Student at ISAE-Supaero*

> *Built with discipline. Executed with precision.*
