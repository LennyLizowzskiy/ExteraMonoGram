# Monogram

![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=android)
![TDLib](https://img.shields.io/badge/TDLib-1.8.61-blue)
![Status](https://img.shields.io/badge/Status-Active_Development-orange)

**A modern, lightning-fast, and elegant unofficial Telegram client for Android.**

> **Note:** Monogram is in **active development**. Expect frequent updates, codebase changes, and bugs.

## Overview

Monogram is built to deliver a native and seamless Telegram experience. Powered by the official **TDLib**, it features a fluid **Material Design 3** interface and follows strict **Clean Architecture** and **MVI** principles.

## Tech Stack

* **Architecture & State:** MVI, [Decompose](https://github.com/arkivanov/Decompose) (navigation & lifecycle), Koin (DI).
* **UI:** Jetpack Compose + Material 3 Adaptive (seamless phone-to-tablet scalability).
* **Async:** Kotlin Coroutines & Flow.
* **Media:** Media3/ExoPlayer (playback), Coil 3 (GIF/SVG/video frames), Lottie (animations).
* **Camera & ML:** CameraX + ML Kit Vision (lightning-fast QR/barcode scanning).
* **Maps:** OSMDroid (open-source native map rendering).
* **Security:** Biometric Compose (app locking), Security Crypto (safe local data storage).

##  Getting Started

**1. Clone the repository**
```bash
git clone https://github.com/monogram-android/monogram.git

```

**2. Set up API Keys**
Create a `local.properties` file in the project root and add your Telegram API credentials (you can grab these from [my.telegram.org](https://my.telegram.org/)):

```properties
API_ID=your_api_id
API_HASH=your_api_hash

```

**3. Build & Run**
Open the project in **Android Studio**, let Gradle sync, and fire it up!

## Contributing

Pull requests and issues are always welcome! Since the project is actively evolving, please open an issue first to discuss any major changes or features you'd like to implement before writing code.