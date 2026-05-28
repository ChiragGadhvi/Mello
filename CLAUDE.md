# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Mello** is a native Android micro-journaling app written in 100% Kotlin with Jetpack Compose. Users capture one-sentence daily reflections (max 150 chars) with mood tracking, streak counting, voice input, and daily push reminders.

- **Namespace:** `com.chirag.mello`
- **Min SDK:** 24 (Android 7.0) | **Target SDK:** 36 (Android 15)
- **Version:** 1.4 (versionCode: 5)

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
│   └── JournalViewModel.kt   # Exposes 8 StateFlows: entries, streak, totalEntries, moodCounts,
│                             #   firstEntryDate, entriesByDay, heatmapData; methods: save/update/delete
├── data/
│   ├── AppDatabase.kt        # Room singleton (thread-safe lazy init); handles v1→v2 migration
│   ├── JournalDao.kt         # DAO: insertEntry, updateEntry, deleteEntry, getAllEntries (Flow),
│   │                         #   getAllTimestamps, deleteAllEntries
│   ├── JournalEntry.kt       # @Entity: id, text, mood, timestamp
│   ├── Mood.kt               # Data class with 6 mood presets (each has drawableRes + Color)
│   └── StreakCalculator.kt   # Pure business logic; computes consecutive journaling days
├── notification/
│   └── ReminderWorker.kt     # WorkManager OneTimeWorkRequest; shows notification, reschedules itself daily
└── ui/
    ├── navigation/
    │   └── MelloNavGraph.kt  # 5 routes: onboarding, home, timeline, insights, profile
    ├── screens/
    │   ├── HomeScreen.kt     # Journal entry UI; mood pager carousel, 150-char input, voice mic button
    │   ├── TimelineScreen.kt # Calendar month view with mood-colored cells; edit/delete via bottom sheet
    │   ├── InsightsScreen.kt # Analytics: mood distribution bar chart, 90-day heatmap, top mood card
    │   ├── ProfileScreen.kt  # User settings, stats, reminder time picker, profile photo
    │   ├── OnboardingScreen.kt
    │   └── MelloBackground.kt # Shared composable: background image with dark overlay
    ├── theme/
    │   ├── Color.kt          # Brand palette: Lavender, Mint, Peach (dark theme only)
    │   ├── Theme.kt          # Material 3 dark color scheme
    │   └── Type.kt           # Poppins (headings) + Nunito (body) via Google Fonts
    └── util/
        └── SpeechHelper.kt   # rememberSpeechRecognizer() composable; wraps Android SpeechRecognizer;
                              #   returns SpeechState with isListening, startListening(), stopListening()
```

### Data Flow

1. **MainActivity** checks `SharedPreferences` → routes nav start to `home` or `onboarding`
2. **MelloNavGraph** controls all screen transitions; bottom nav has 4 tabs (Log, Journal, Insights, Profile)
3. **JournalViewModel** collects `JournalDao.getAllEntries()` Flow → derives all analytics state (streak, mood counts, heatmap, calendar grouping) and exposes everything via `collectAsState()`
4. **SpeechHelper** is used in HomeScreen and TimelineScreen edit dialogs; appends recognized text to the current input (respects 150-char cap)
5. **ReminderWorker** fires once daily at the user-configured hour/minute, posts a notification, then enqueues a new `OneTimeWorkRequest` for the next day

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
- **Google Fonts** `1.6.7`

When adding new dependencies, add versions to `libs.versions.toml` first, then reference them in `app/build.gradle.kts`.

## Key Manifest Permissions

- `POST_NOTIFICATIONS` — required at runtime on Android 13+; requested in `MainActivity`
- `RECORD_AUDIO` — required for voice input via `SpeechHelper`
- `RECEIVE_BOOT_COMPLETED` — allows WorkManager to reschedule reminders after device reboot
- `WAKE_LOCK` — used by WorkManager for reliable background execution
- `INTERNET` — declared for network access
