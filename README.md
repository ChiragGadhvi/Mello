<h1 align="center">
  <img src="app/src/main/res/drawable/icon.png" width="128" alt="Mello Logo">
  <br/>
  Mello 🌙
</h1>

<p align="center">
  <b>Capture your day in one line.</b><br/>
  A beautifully designed, minimalist Android journaling app that helps you reflect on your feelings without the pressure of writing long paragraphs.
</p>

---

## ✨ Features

- **Micro-Journaling**: Capture your day in exactly one sentence. (Max 150 characters).
- **Mood Tracking**: Log how you feel every day using expressive emojis.
- **Daily Reminders**: Gentle, customizable daily notifications (powered by WorkManager) so you never miss a day.
- **Streak Tracking**: Keep your momentum going! Mello tracks your consecutive journaling days to help build a lasting habit.
- **Premium UI/UX**: Built entirely with Jetpack Compose featuring glassmorphic design elements, smooth spring animations, and an elegant layout.

## 🛠️ Tech Stack

Mello is built using modern Android development practices:

- **100% Kotlin**
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design guidelines.
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room) for persistent offline data storage.
- **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable daily reminder notifications.
- **Navigation**: Compose Navigation.
- **Asynchrony**: Kotlin Coroutines & Flows.

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable version recommended)
- SDK 36 (Minimum supported SDK is 24 - Android 7.0)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/ChiragGadhvi/Mello.git
   ```
2. **Open in Android Studio**
   Open Android Studio, select `File > Open`, and choose the cloned `Mello` directory.
3. **Sync Gradle**
   Wait for Android Studio to sync the project dependencies.
4. **Build and Run**
   Connect your Android device or start an emulator and click the Run button (`Shift + F10`).

## 🎨 Design

Mello places a strong emphasis on a calming, premium aesthetic. It features dynamic gradients, soft drop shadows, and delicate animations that make interacting with the app feel satisfying and tranquil. 
- *Colors*: Lavender, Mint, Peach, and Surface variants for Dark/Light mode adaptability.

## 📱 Permissions

- **Post Notifications (Android 13+)**: Requested on the first launch so Mello can deliver your daily check-in reminder.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/ChiragGadhvi/Mello/issues) if you want to contribute.

## 📝 License

This project is open-source and available under the standard MIT License.
