# BhoomiSetu (భూమి సేతు)

> **A Comprehensive Digital Land Governance, Survey Marketplace & Verification Platform**  
> *Bridging Citizens, Licensed Surveyors, and Legal Experts with Transparent Land Services in Andhra Pradesh.*

---

## 📌 Project Overview

**BhoomiSetu** is a modern, native Android application built using **Jetpack Compose** and **Kotlin**, backed by a resilient **Node.js + Express + MongoDB** backend. The platform empowers citizens, farmers, and property owners with direct access to official government land verification portals, a transparent marketplace for hiring licensed surveyors, and verified access to certified legal advisors and document writers.

---

## ✨ Key Features & Architecture

### 1. 🏛️ MeeBhoomi & AP IGRS Official Portals
- **Direct Portal Access**: One-tap access to official Andhra Pradesh government portals via secure Chrome Custom Tabs (with automatic intent fallback).
- **MeeBhoomi Integration**: Verify land records, 1-B extracts, Adangal, and village survey maps directly from `meebhoomi.ap.gov.in`.
- **AP IGRS EC Verification**: Search Encumbrance Certificates (EC) and verify property registration titles securely from `registration.ec.ap.gov.in`.
- **Zero Redundant Overhead**: Streamlined access without requiring manual local parcel registration before accessing government portals.

### 2. 🗺️ Survey Request & Marketplace
- **Smart Location Cascading**: Hierarchical scroll-down dropdowns covering all 26 Andhra Pradesh districts, mandals, and villages.
- **Auto-Fill & Common Parcel Suggestions**: Instant selection of survey formats or saved parcels.
- **Transparent Job Lifecycle**: Real-time statuses (`OPEN`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`).
- **Surveyor Feed**: Real-time push notifications via Socket.io when new surveys in the surveyor's district are requested.
- **In-App Communication**: Real-time socket-based messaging between citizens and assigned surveyors.

### 3. ⚖️ Legal Advisory & Document Writers
- **Verified Expert Directory**: Browse certified legal practitioners specializing in property law, title disputes, and land succession.
- **Document Preparation**: Connect with licensed document writers for sale deeds, gift deeds, partitions, and mortgage registrations.
- **Appointment Scheduling**: Direct consultation booking and order management.

### 4. 🔐 Security, Session Management & Role-Based Access Control (RBAC)
- **Role Profiles**: Support for `Citizen`, `Surveyor`, `Legal Advisor`, and `Document Writer`.
- **Silent JWT Auto-Refresh**: Powered by OkHttp `Authenticator` with token rotation (`accessToken` 7d, `refreshToken` 30d) ensuring zero unexpected session expirations while browsing.
- **Biometric & Secure Storage**: Encrypted token persistence using Jetpack DataStore.

### 5. 🌐 Multilingual Accessibility
- **Full Bilingual Support**: Instant toggle between **English** and **Telugu (తెలుగు)** across all screens, inputs, and error dialogs.

---

## 🛠️ Technology Stack

### Android App
- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (Presentation, Domain, Data layers) + MVI/MVVM pattern
- **Dependency Injection**: Dagger Hilt
- **Asynchronous & Reactive**: Kotlin Coroutines & Flow (StateFlow, SharedFlow)
- **Networking**: Retrofit 2, OkHttp 3, Gson, Socket.io Client
- **Local Persistence**: Jetpack Room Database, Jetpack DataStore Preferences
- **Web Navigation**: AndroidX Browser (Chrome Custom Tabs)

### Backend API
- **Runtime**: Node.js & Express.js
- **Database**: MongoDB with Mongoose ODM (Survey Requests, Orders, Chat Messages)
- **Real-Time Communication**: Socket.io
- **Authentication**: JSON Web Tokens (JWT) & bcrypt password hashing
- **Mailing**: Google Apps Script Webhook & Brevo REST API

---

## 📁 Repository Structure

```
BhoomiSetu/
├── app/                                    # Android Application Module
│   └── src/main/java/com/satya/bhoomisetu/
│       ├── data/                           # Data layer (Remote API, Local DB, Repositories)
│       │   ├── local/                      # Room entities & DAOs
│       │   ├── remote/                     # Retrofit API services, DTOs, TokenAuthenticator
│       │   └── repository/                 # Repository implementations
│       ├── di/                             # Dagger-Hilt dependency injection modules
│       ├── domain/                         # Domain layer (Use cases, Domain models, Interfaces)
│       │   ├── model/                      # Business models
│       │   ├── repository/                 # Repository contracts
│       │   └── usecase/                    # Pure domain logic use cases
│       ├── presentation/                   # Presentation layer (Jetpack Compose UI)
│       │   ├── auth/                       # Login, Registration, OTP screens
│       │   ├── home/                       # Unified Dashboard
│       │   ├── property/                   # MeeBhoomi & AP IGRS Portal screens
│       │   ├── survey/                     # Survey Request Form, Marketplace, Details
│       │   ├── advisor/                    # Legal Advisor directory & booking
│       │   ├── writer/                     # Document Writer services
│       │   ├── navigation/                 # Jetpack Compose Navigation Graph
│       │   └── theme/                      # Material 3 Design System & Typography
│       └── util/                           # Utilities, Location Data, Localization
├── backend/                                # Node.js / Express Backend Service
│   ├── middleware/                         # JWT Auth & Validation middlewares
│   ├── models/                             # Mongoose schemas (Requests, Chats)
│   ├── routes/                             # Express REST API routes
│   ├── utils/                              # Mailer & helper utilities
│   ├── server.js                           # Express entry point & Socket.io handler
│   └── package.json                        # Node dependencies
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Hedgehog or newer
- **JDK**: Java 17+ (or bundled Android Studio JBR)
- **Node.js**: v18+ (for local backend)
- **MongoDB**: Community Edition or MongoDB Atlas URI

### 1. Running the Android Application
1. Open Android Studio and select **Open**, then choose the `BhoomiSetu` root directory.
2. Allow Gradle to sync dependencies.
3. Select an Android Emulator or physical device running Android 8.0 (API level 26) or higher.
4. Click **Run** (`Shift + F10`).

### 2. Running the Backend Service (Optional for local testing)
```bash
cd backend
npm install
npm start
```
*The backend server will launch on port `5000` (or `PORT` specified in `.env`).*

---

## 👥 Contributors & Academic Submission

- **Author**: Satya Ganesh ([@Sg3107](https://github.com/Sg3107))
- **Project**: BhoomiSetu (Digital Land Governance System)
- **Target Audience**: Citizens, Farmers, Licensed Surveyors & Legal Authorities of Andhra Pradesh

---

## 📄 License
This project is submitted for academic evaluation and research purposes.
