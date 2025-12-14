# Personal Leveling System - App Structure & Functionality

## Overview
**Personal Leveling System** is an Android application designed to gamify personal development. It tracks various aspects of the user's life—fitness, health, and daily habits—treating them as "missions" or "stats" to level up the user, similar to an RPG character.

## Key Features
1.  **User Profile & Leveling**: Tracks user stats, experience points (XP), and potentially levels.
2.  **Training Management**:
    -   **Endurance & Flexibility**: Dedicated tracking for specific training types.
    -   **Sessions & Sets**: Detailed logging of workout sessions, exercises, and sets.
    -   **Programs**: Organized training programs.
3.  **Health & Lifestyle Tracking**:
    -   **Water Intake**: Logs daily water consumption.
    -   **Sleep Monitoring**: Tracks sleep duration and quality.
    -   **Nutrition**: Manages meals and nutrition data.
4.  **Missions System**:
    -   **Daily & Weekly Missions**: Automated tasks to encourage consistent habits.
    -   **Mission Resets**: Handled by background receivers/schedulers.
5.  **Data Synchronization**:
    -   **Google Integration**: Syncs data with Google Drive/Datastore and potentially Google Calendar.
    -   **Backup & Restore**: Functionality to upload/download data.

## Technical Architecture
The app follows a standard **MVVM (Model-View-ViewModel)** architecture pattern on Android.

### 1. Technology Stack
-   **Language**: Kotlin
-   **UI Framework**: Hybrid approach using **XML Layouts** (View System) and **Jetpack Compose**.
-   **Dependency Injection**: Manual injection (evident in `MainActivity` manually creating Repositories/ViewModels).
-   **Local Database**: **Room** (Android Jetpack) for SQLite abstraction.
-   **Background Processing**: **WorkManager** for scheduled tasks (notifications, resets) and `BroadcastReceiver` for alarm-based events.
-   **Networking/Sync**: Google API Client (Drive, Calendar), OkHttp, Retrofit (likely for other APIs).
-   **Charting**: `MPAndroidChart` and `GraphView` for visualizing progress.

### 2. Project Structure (`app/src/main/java/...`)

#### `com.example.personallevelingsystem` (Root Package)
-   `MainActivity.kt`: The main entry point.
    -   Sets up the **Navigation Drawer** and **ViewPager2** (likely for a dashboard carousel).
    -   Initializes the `AppDatabase` and triggers background workers/schedulers.
    -   Handles navigation to specific feature Activities (Profile, Training, etc.).
-   `MyApplication.kt`: Application-level context class.

#### Key Sub-packages
-   `model/`: Data classes (Entities).
    -   `User`, `Mission`, `TrainingSession`, `Exercise`, `Sleep`, `Water`, etc.
    -   These represent the tables in the database.
-   `data/`: Data access layer.
    -   `AppDatabase.kt`: Room database definition.
    -   **DAOs**: Likely contains Data Access Objects for accessing tables.
-   `repository/`: Business logic and data abstraction.
    -   `MissionRepository`, `UserRepository`: Mediates between the Database/Network and the UI.
-   `ui/`: User Interface layer.
    -   Contains Activities (e.g., `UserProfileActivity`, `TrainingActivity`, `WaterActivity`) and potentially Fragments/Composables.
    -   Responsible for displaying data and handling user interactions.
-   `viewmodel/`: (Presumed) ViewModels that hold UI state and communicate with Repositories.
-   `worker/` & `scheduler/` & `receiver/`: Background tasks.
    -   `DailyNotificationWorker`: Periodic WorkManager task.
    -   `DailyMissionResetReceiver`: BroadcastReceiver for timing mission resets.
    -   `MissionScheduler`: Helper to schedule these alarms.
-   `datastore/`:
    -   `DatastoreUploader`, `DatastoreSyncManager`: Handles logic for syncing local database data to the cloud (Google Datastore/Drive).

### 3. Data Flow
1.  **User Input**: User interacts with an Activity (e.g., adds water in `WaterActivity`).
2.  **UI Layer**: The Activity/Fragment calls methods in a `ViewModel` or directly uses a `Dao`/`Repository`.
3.  **Data Layer**:
    -   For local data: usage of Room DAOs (`WaterDao`).
    -   For sync: `DatastoreUploader` reads from DAOs and pushes to the cloud.
4.  **Persistence**: Data is stored in the local SQLite database via Room.

## Configuration
-   **Gradle**:
    -   `build.gradle.kts`: Defines dependencies (Room, Compose, Google APIs) and Android SDK versions (minSdk 23, compileSdk 34).
-   **Manifest**:
    -   Declares all Activities, Permissions (Internet, Accounts, Notifications), and Receivers.
