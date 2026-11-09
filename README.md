# Chutur Paan: Android customer app

A native Android app for ordering paan for **pickup**. Browse the menu, customise an item, pay, then track the
order live and collect it in the shop with a QR pickup code. There is no delivery.

> Package: `com.paan.safa` · Languages: English, Bengali (বাংলা), Odia (ଓଡ଼ିଆ)

## Features

- Phone number login with OTP, then profile completion
- Menu with categories, product detail and item options
- Cart, checkout, optional order comment (max 200 characters)
- Online payment through a payment gateway
- Live order status timeline: Pending, Making, Ready, Completed (or cancelled by the shop)
- Pickup QR code and in-app QR scanner
- Push notifications for order updates (Firebase Cloud Messaging) with an in-app notification list
- In-app language switching

## Tech stack

| | |
|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | MVVM, repository pattern, `StateFlow` |
| DI | Hilt |
| Navigation | Navigation Compose |
| Networking | Retrofit, OkHttp, kotlinx.serialization |
| Images | Coil |
| Push | Firebase Cloud Messaging |
| Scanner | Google ML Kit code scanner |
| Local storage | DataStore |

The app talks to a separate Laravel REST API backend.

## Getting started

### Requirements

- Android Studio (latest stable) and JDK 17
- An Android device or emulator, Android 7.0+ (check `minSdk` in `app/build.gradle.kts`)

### Setup

1. Clone the repo.
2. Add your Firebase config: download `google-services.json` from your Firebase project into `app/`.
   It is git-ignored, so each developer supplies their own.
3. Set the backend URL: the app reads `BuildConfig.API_BASE_URL`, defined as a `buildConfigField` in
   `app/build.gradle.kts`. Point it at your API.
4. Open in Android Studio, let Gradle sync, then Run.

Feature flags live in `app/src/main/java/com/paan/safa/core/util/AppConfig.kt`.

## Building release APKs

One command builds, verifies and names the 32-bit, 64-bit and universal APKs:

```powershell
powershell -ExecutionPolicy Bypass -File tools\build-release.ps1 -VersionName 1.1.0
```

Results land in `dist\` together with `SHA256SUMS.txt`. Full setup (keystore, Gradle splits, GitHub release)
is in [docs/RELEASE.md](docs/RELEASE.md).

| APK | Use when |
|---|---|
| `...-v8a-64bit.apk` | Modern phones (64-bit ARM) |
| `...-v7a-32bit.apk` | Older 32-bit ARM phones |
| `...-universal.apk` | Unsure, works everywhere (bigger) |

## Project structure

```
app/src/main/java/com/paan/safa/
├── core/        navigation, localization, utilities, UI helpers
├── data/        local (DataStore), remote (Retrofit), repositories
├── di/          Hilt modules
├── domain/      models and repository interfaces
├── push/        FCM service, notification channels, device token
└── ui/          components, theme, screens (home, cart, checkout, order, profile, ...)
tools/           build-release.ps1
docs/            RELEASE.md
```

## Security

- `keystore.properties`, `*.jks` and `google-services.json` are git-ignored. Never commit them.
- Keep a backup of your release keystore; updates must be signed with the same key.
- Verify downloads against `SHA256SUMS.txt`.

## Author

**Dharanidhar Mahata** ([imdevops](https://imdevops.in)) · GitHub: [GitHubsantu](https://github.com/GitHubsantu)

## License

Released under the [MIT License](LICENSE).
