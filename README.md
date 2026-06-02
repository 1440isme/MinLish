# MinLish - Advanced Vocabulary Learning Platform

MinLish is a modern, high-performance vocabulary learning application tailored for TOEIC and IELTS preparation. Built on the **SM-2 Spaced Repetition Algorithm**, it helps learners commit vocabulary to long-term memory efficiently through smart review card schedules, custom study sessions, and interactive practice quizzes.

The system is composed of two primary modules:
1. **Android App**: A clean Jetpack Compose client following unidirectional data flow and modular MVVM architecture.
2. **NestJS Backend**: A robust REST API backed by NestJS 11, Prisma ORM 7.x, and MySQL/MariaDB.

---

## Table of Contents

- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Backend Setup](#1-backend-setup)
  - [2. Android App Setup](#2-android-app-setup)
- [System Architecture](#system-architecture)
  - [Data Flow (Unidirectional Data Flow)](#data-flow-unidirectional-data-flow)
  - [Android Architecture](#android-architecture)
  - [Backend Architecture](#backend-architecture)
- [Environment Variables & Configuration](#environment-variables--configuration)
  - [Backend Environment Variables (.env)](#backend-environment-variables-env)
  - [Android Local Configuration (local.properties)](#android-local-configuration-localproperties)
- [Available Scripts](#available-scripts)
- [Testing](#testing)
- [Production Deployment Checklist](#production-deployment-checklist)
- [Troubleshooting](#troubleshooting)

---

## Key Features

* **SM-2 Spaced Repetition Algorithm**: Automatic due-date calculations based on user rating reviews (`AGAIN`, `HARD`, `GOOD`, `EASY`). Centralized entirely on the backend to enforce mathematical consistency.
* **System & Custom Decks**: Multi-tiered vocabulary organization (such as TOEIC 600+, IELTS 6.5+) alongside custom decks created by users.
* **Smart Favorites Flow**: Dedicated favorites list implemented as a default user deck with `source_vocabulary_id` tracking, ensuring duplicate checks and smooth synchronization.
* **Batch CSV Imports**: Support for uploading custom vocabularies with partial success tracking, skipping duplicates, and batch database operations.
* **Interactive Practice Quiz Engine**: Dynamically generated test sessions with multiple choice, listening, and fill-in-the-blank question types.
* **Dashboard Analytics**: Streak, daily learning progress, and practice history synced to the Cloud.
* **Google Sign-In**: Simple login and automatic account registration backed by secure Google ID token verification.

---

## Tech Stack

### Android Client
* **Language**: Kotlin 2.x
* **UI Framework**: Jetpack Compose (Material 3)
* **Architecture**: MVVM with Unidirectional Data Flow (UDF)
* **State Management**: Kotlin StateFlow & Coroutines
* **Dependency Injection**: Manual Dependency Injection via centralized Application-level Factory
* **Networking**: Retrofit 2 & OkHttp 3 with JWT Authenticator & Interceptor
* **Persistence**: Jetpack Preferences DataStore

### NestJS Backend
* **Framework**: NestJS 11 (Node.js)
* **ORM**: Prisma ORM 7.x (with `@prisma/adapter-mariadb`)
* **Database**: MySQL / MariaDB
* **API Documentation**: Swagger (OpenAPI 3.0)
* **Authentication**: Passport.js with JWT Strategy & Local Strategy
* **Security**: Bcrypt password hashing, DTO validation (`class-validator`), and ownership verification guards

---

## Project Directory Structure

```text
MinLish/
├── android/                   # Kotlin + Compose Mobile App
│   ├── app/
│   │   ├── src/main/java/com/minlish/
│   │   │   ├── core/          # App shell, navigation, shared presentation & data sources
│   │   │   ├── feature/       # Feature-sliced folders (auth, home, deck, learning, practice, profile, settings)
│   │   │   └── ui/theme/      # Visual tokens, colors, typography, shapes
│   │   └── build.gradle.kts   # Module gradle build script
│   └── settings.gradle.kts    # Gradle project structure settings
└── backend/                   # NestJS Backend API
    ├── prisma/                # Prisma Schema & Database Seeds
    └── src/
        ├── config/            # Common guards, decorators, errors, filters
        ├── entities/          # Shared database entities & Swagger decorators
        └── modules/           # Modular services (auth, users, decks, vocabularies, review, practice, analytics, notifications)
```

---

## Prerequisites

Before starting, ensure you have the following installed on your machine:
* **Java Development Kit (JDK)**: Version 21 (Adoptium/Temurin recommended)
* **Android SDK & Build Tools**: Android 14+ (API level 34+)
* **Node.js**: Version 18.x or 20.x (LTS)
* **Database**: MariaDB 10.x or MySQL 8.x
* **Package Manager**: `npm` (standard)

---

## Getting Started

### 1. Backend Setup

#### **Step 1: Install Dependencies**
Navigate to the backend directory and install all node packages:
```bash
cd backend
npm install
```

#### **Step 2: Database Initialization**
Create an empty database in your MariaDB/MySQL server named `minlish`:
```sql
CREATE DATABASE minlish CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### **Step 3: Environment Variables Setup**
Copy the sample environment file and configure it:
```bash
cp .env.example .env
```
Ensure your `DATABASE_URL` matches your local server credentials:
```env
DATABASE_URL="mysql://root:password@localhost:3306/minlish"
```

#### **Step 4: Seed Database and Run Migrations**
Run the custom database reset command to perform structural migration and seed critical system data (categories, default tracks, levels, system decks, and core vocabulary lists):
```bash
npm run db:reset
```

#### **Step 5: Run the Development Server**
Start the NestJS backend in development mode:
```bash
npm run start:dev
```
The server will boot up and start listening on [http://localhost:3000](http://localhost:3000).

---

### 2. Android App Setup

#### **Step 1: Configure Local Variables**
Create a `local.properties` file inside the `android/` directory (if it does not exist) and configure the API endpoint and Google login credentials:
```properties
sdk.dir=/path/to/your/android/sdk
api.baseUrl=http://10.0.2.2:3000/
google.webClientId=YOUR_GOOGLE_WEB_CLIENT_ID
```
*(Note: `10.0.2.2` is the standard loopback address mapping to your machine's localhost from inside the Android Emulator).*

#### **Step 2: Build the Application**
Compile the app using Gradle. The project uses standard **Adoptium JDK 21** as its compiler toolchain:
```bash
cd android
./gradlew compileDebugKotlin --no-daemon
```

#### **Step 3: Run the App**
Open the `android` folder in **Android Studio**, start your preferred Virtual Device (AVD Emulator), and click **Run** (or execute `./gradlew installDebug`).

---

## System Architecture

### Data Flow (Unidirectional Data Flow)

The application adheres to unidirectional data flow (UDF) to keep state changes predictable:
```text
User Event (UI Screen) -> Trigger Method (ViewModel) -> Call Service (Repository) -> HTTP API Request
                                                                                          │
UI State (Flow.collect) <- Recompose Screen <- Expose Read-Only StateFlow <- Success Response/DB Cache
```

### Android Architecture

```text
             [ UI Layer ] (Compose Screens)
                  │
                  ▼
          [ ViewModel Layer ] (Exposes StateFlows, manages view scopes)
                  │
                  ▼
          [ Repository Layer ] (Single source of truth)
                  │
          ┌───────┴───────┐
          ▼               ▼
     [ Network ]   [ Preferences ] (DataStore)
    (Retrofit APIs)
```

### Backend Architecture

```text
    [ Client Request ]
            │
            ▼
     [ Controllers ] (Defines routes, schema validation)
            │
            ▼
      [ Services ] (Encapsulates core business invariants & SM-2 algorithms)
            │
            ▼
     [ Prisma Client ] (Handles DB mappings and database connections)
            │
            ▼
    [ MySQL/MariaDB ]
```

---

## Environment Variables & Configuration

### Backend Environment Variables (`.env`)

| Variable | Description | Default | Required for Prod |
| :--- | :--- | :--- | :--- |
| `DATABASE_URL` | MySQL/MariaDB connection URL | `mysql://...` | **Yes** |
| `PORT` | Listening port for NestJS server | `3000` | No |
| `JWT_SECRET` | Secret key used to sign Auth tokens | `secretKey` | **Yes** (Use strong secret) |
| `GOOGLE_CLIENT_ID` | OAuth2 Client ID for Google token validation | - | **Yes** (If Google login is used) |
| `ENABLE_MOCK_GOOGLE_LOGIN` | Allows test accounts to skip Google check | `true` (Dev) | **No** (Set to `false` in Prod) |

### Android Local Configuration (`local.properties`)

| Key | Description | Example |
| :--- | :--- | :--- |
| `api.baseUrl` | Base API URL of your backend module | `http://10.0.2.2:3000/` (Emulator) |
| `google.webClientId` | The Web Client ID from Google API Console | `12345678-abc.apps.googleusercontent.com` |

---

## Available Scripts

### NestJS Backend Scripts (`backend/`)

| Script | Command | Description |
| :--- | :--- | :--- |
| `npm run start:dev` | `nest start --watch` | Starts NestJS server in watch mode |
| `npm run build` | `nest build` | Compiles application code into `dist/` |
| `npm run start:prod` | `node dist/main` | Runs the compiled production code |
| `npm run db:reset` | `prisma migrate reset` | Wipes DB, applies migrations, and seeds standard tables |
| `npm run db:migrate` | `prisma migrate dev` | Creates and runs a local schema migration |
| `npm run db:studio` | `prisma studio` | Opens database editor GUI at [http://localhost:5555](http://localhost:5555) |
| `npm run lint` | `eslint ... --fix` | Formats and fixes code styling rules |

### Android Gradle Scripts (`android/`)

| Command | Description |
| :--- | :--- |
| `./gradlew compileDebugKotlin` | Compiles only Kotlin source code in debug configurations |
| `./gradlew assembleDebug` | Builds the debug APK file (`.apk`) |
| `./gradlew installDebug` | Installs the debug build onto the active emulator/device |
| `./gradlew clean` | Deletes build outputs and build directories |
| `./gradlew signingReport` | Displays signing fingerprints (useful to get local debug SHA-1 for Google Login) |

---

## Testing

### Run Backend Tests
Run Jeset tests in the backend repository:
```bash
cd backend
npm run test          # Unit tests
npm run test:e2e      # End-to-end API tests
npm run test:cov      # Generate test coverage reports
```

---
