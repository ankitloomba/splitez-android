# SplitEZ — Android App

Native Android app for SplitEZ, built with **Kotlin** and **Jetpack Compose** (Material 3).

## Requirements

- Android Studio Hedgehog+
- Kotlin 2.0
- Min SDK 26 (Android 8.0)
- Target SDK 34

## Architecture

```
app/src/main/java/com/splitezapp/
├── MainActivity.kt           # Entry point + navigation
├── data/
│   ├── api/
│   │   ├── ApiService.kt     # Retrofit interface (all endpoints)
│   │   └── ApiClient.kt      # Retrofit setup, auth interceptor, encrypted prefs
│   └── models/
│       └── Models.kt         # All data classes matching API
├── ui/
│   ├── auth/                 # Login, Register screens + ViewModel
│   ├── home/                 # Home dashboard (balances, activity)
│   ├── groups/               # Groups list, create dialog
│   ├── trips/                # Trips list, create dialog
│   ├── finances/             # Income, personal expenses, summary
│   ├── settings/             # Profile, logout
│   ├── components/           # Shared: AvatarView, EmptyState, LoadingScreen
│   └── theme/                # Material 3 theme, colors
```

## Features

- ✅ Email + password authentication (register, login, forgot password)
- ✅ Encrypted token storage (EncryptedSharedPreferences)
- ✅ Home dashboard with balances & activity feed
- ✅ Groups — list, create
- ✅ Trips — list, create
- ✅ My Finances — income, personal expenses, monthly summary
- ✅ Settings — profile view, logout
- ✅ Material 3 design with dark mode support
- ✅ Deterministic avatar colors from user ID

## Dependencies

- Retrofit 2.11 + Gson — networking
- OkHttp 4.12 — HTTP client
- Jetpack Compose BOM 2024.06 — UI
- AndroidX Security Crypto — encrypted storage

## API

Points to `https://splitez-backend-production.up.railway.app/api`
