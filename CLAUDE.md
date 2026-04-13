# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Mello** is a native Android micro-journaling app written in 100% Kotlin with Jetpack Compose. Users capture one-sentence daily reflections (max 150 chars) with mood tracking, streak counting, and daily push reminders.

- **Namespace:** `com.chirag.mello`
- **Min SDK:** 24 (Android 7.0) | **Target SDK:** 36 (Android 15)
- **Version:** 1.2 (versionCode: 3)

## Build & Run Commands

```bash
# Build
./gradlew build

# Clean build
./gradlew clean build

# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew test --tests "com.chirag.mello.ExampleUnitTest"

# Install on connected device
./gradlew installDebug
```

In Android Studio: **Shift + F10** to run, **Shift + F9** to debug.

## Architecture

MVVM pattern with Jetpack Compose UI, Room for persistence, and WorkManager for background tasks.

```
app/src/main/java/com/chirag/mello/
├── MainActivity.kt            # Entry point; requests POST_NOTIFICATIONS, schedules ReminderWorker
├── viewmodel/
│   └── JournalViewModel.kt   # Exposes StateFlow<List<JournalEntry>> and streak count
├── data/
│   ├── AppDatabase.kt        # Room singleton (thread-safe lazy init); handles v1→v2 migration
│   ├── JournalDao.kt         # DAO: insert, delete, getAllEntries (Flow)
│   ├── JournalEntry.kt       # @Entity: id, text, mood, timestamp
│   ├── Mood.kt               # Enum with 6 moods (each has an emoji + display name)
│   └── StreakCalculator.kt   # Pure business logic; computes consecutive journaling days
├── notification/
│   └── ReminderWorker.kt     # WorkManager OneTimeWorkRequest; shows notification, reschedules itself daily
└── ui/
    ├── navigation/
    │   └── MelloNavGraph.kt  # 4 routes: onboarding, home, timeline, profile
    ├── screens/
    │   ├── HomeScreen.kt     # Main journal entry UI
    │   ├── TimelineScreen.kt # Scrollable history of all entries
    │   ├── ProfileScreen.kt  # User settings + reminder time picker
    │   ├── OnboardingScreen.kt
    │   └── MelloBackground.kt # Shared gradient background composable (glassmorphic design)
    └── theme/
        ├── Color.kt          # Brand palette: Lavender, Mint, Peach
        ├── Theme.kt          # Material 3 theme
        └── Type.kt           # Poppins + Nunito fonts via Google Fonts
```

### Data Flow

1. **MainActivity** checks `SharedPreferences` → routes nav start to `home` or `onboarding`
2. **MelloNavGraph** controls all screen transitions; bottom nav has 3 tabs (Log, Journal, Profile)
3. **JournalViewModel** collects `JournalDao.getAllEntries()` Flow → exposes to Compose screens via `collectAsState()`
4. **ReminderWorker** fires once daily at the user-configured hour/minute, posts a notification, then enqueues a new `OneTimeWorkRequest` for the next day

### State Persistence

| What | Where |
|---|---|
| Journal entries | Room DB (SQLite) |
| Onboarding complete flag | `SharedPreferences` key `onboarding_complete` |
| Reminder time | `SharedPreferences` keys `reminder_hour` / `reminder_minute` |
| User name + profile photo URI | `SharedPreferences` keys `user_name` / `dp_uri` |

### Dependency Catalog

All dependency versions are centralized in `gradle/libs.versions.toml`. Key libraries:

- **Jetpack Compose BOM** `2024.09.00`
- **Room** `2.8.4`
- **WorkManager** `2.9.0`
- **Navigation Compose** `2.7.7`
- **Kotlin Coroutines** `1.7.3`
- **Lottie** `6.4.0`

When adding new dependencies, add versions to `libs.versions.toml` first, then reference them in `app/build.gradle.kts`.

## Key Manifest Permissions

- `POST_NOTIFICATIONS` — required at runtime on Android 13+; requested in `MainActivity`
- `RECEIVE_BOOT_COMPLETED` — allows WorkManager to reschedule reminders after device reboot
- `WAKE_LOCK` — used by WorkManager for reliable background execution
