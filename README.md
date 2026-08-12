# 🏙️ CivicTracker

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=flat&logo=supabase&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)
![Gemini](https://img.shields.io/badge/Gemini%20API-8E75B2?style=flat&logo=googlegemini&logoColor=white)

**CivicTracker** is a native Android app that makes it simple for citizens to report the everyday civic problems they run into — potholes, broken streetlights, garbage pileups, water leaks, and more — and track how those reports get resolved.

Built with Kotlin and Jetpack Compose, backed by Supabase and Firebase Auth, with an AI-assisted issue check powered by Gemini.

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#️-tech-stack)
- [App Flow & Screenshots](#-app-flow--screenshots)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Roadmap](#️-roadmap)
- [Contributors](#-contributors)
- [License](#-license)

---

## ✨ Features

### 📝 Reporting an Issue
- Report a civic issue with a title, category, and detailed description
- Attach a photo two ways — **capture with the camera** or **pick from the gallery**
- **AI Smart Check** — the report is automatically analyzed using Google's Gemini model to help validate, categorize, and flag priority before submission
- Location is auto-attached to every report
- Submit the official report once everything looks right
- **Delete** a report you've submitted, if needed

### 🗺️ Map
- View reported issues plotted on a live map, so citizens and officers can see what's happening nearby

### 📊 Public Scoreboard
- A public, transparent view of community stats — issues resolved vs. pending, so anyone can see how things are progressing

### 🧑‍💼 Officer Dashboard
- A dedicated space for civic officials to review incoming reports, manage their status, and track resolution

### 📌 Issue Tracking
- Citizens can browse other reports in their area and see which ones are **resolved** and which are still **pending**

### 🔐 Secure Auth
- User accounts and sessions handled via Firebase Authentication

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | [Kotlin](https://kotlinlang.org/) |
| UI | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| Backend / Database | [Supabase](https://supabase.com/) |
| Authentication | [Firebase Auth](https://firebase.google.com/products/auth) |
| AI Analysis | [Gemini API](https://ai.google.dev/) |
| Build System | Gradle (Kotlin DSL) |

---

## 📱 App Flow & Screenshots

### 🧑‍🤝‍🧑 Citizen Flow

<table>
  <tr>
    <td align="center"><img src="citizen/01-login.png" width="180"/><br/><b>1. Login</b></td>
    <td align="center"><img src="citizen/02-otp.png" width="180"/><br/><b>2. OTP Verification</b></td>
    <td align="center"><img src="citizen/03-home.png" width="180"/><br/><b>3. Home Page</b></td>
    <td align="center"><img src="citizen/04-report-photo-title.png" width="180"/><br/><b>4. Report — Photo & Title</b></td>
  </tr>
  <tr>
    <td align="center"><img src="citizen/05-report-details.png" width="180"/><br/><b>5. Report — Details</b></td>
    <td align="center"><img src="citizen/06-scoreboard.png" width="180"/><br/><b>6. Public Scoreboard</b></td>
    <td align="center"><img src="citizen/07-map.png" width="180"/><br/><b>7. Map View</b></td>
    <td align="center"><img src="citizen/08-scoreboard.png" width="180"/><br/><b>8. Scoreboard (revisit)</b></td>
  </tr>
</table>

### 🧑‍💼 Officer Flow

> *Steps 1–2 reuse the same login/OTP screens as the citizen flow, since authentication is shared across both roles.*

<table>
  <tr>
    <td align="center"><img src="citizen/01-login.png" width="180"/><br/><b>1. Login</b><br/><sub>(shared with citizen)</sub></td>
    <td align="center"><img src="citizen/02-otp.png" width="180"/><br/><b>2. OTP Verification</b><br/><sub>(shared with citizen)</sub></td>
    <td align="center"><img src="officer/03-home.png" width="180"/><br/><b>3. Home Page</b></td>
    <td align="center"><img src="officer/04-dashboard.png" width="180"/><br/><b>4. Officer Dashboard</b></td>
  </tr>
  <tr>
    <td align="center"><img src="officer/05-profile.png" width="180"/><br/><b>5. Profile Page</b></td>
    <td align="center"><img src="officer/06-dashboard.png" width="180"/><br/><b>6. Dashboard (revisit)</b></td>
    <td align="center"><img src="officer/07-resolved.png" width="180"/><br/><b>7. Mark as Resolved</b></td>
    <td align="center"><img src="officer/08-home.png" width="180"/><br/><b>8. Home Page</b></td>
  </tr>
  <tr>
    <td align="center"><img src="officer/09-scoreboard.png" width="180"/><br/><b>9. Public Scoreboard</b></td>
    <td align="center"><img src="officer/10-scoreboard-result.png" width="180"/><br/><b>10. Scoreboard Result</b></td>
    <td></td>
    <td></td>
  </tr>
</table>

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- JDK 17+
- A [Supabase](https://supabase.com/) project (URL + anon key)
- A [Firebase](https://firebase.google.com/) project with Authentication enabled
- A [Gemini API](https://ai.google.dev/) key

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/anjalikhonde/CivicTracker.git
   cd CivicTracker
   ```

2. **Add your local configuration**

   Create a `local.properties` file in the project root (this is git-ignored) and add your keys:
   ```properties
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   GEMINI_API_KEY=your_gemini_api_key
   ```

3. **Add your Firebase config**

   Download `google-services.json` from your Firebase console and place it in the `app/` directory.

4. **Build and run**

   Open the project in Android Studio and run it on an emulator or physical device, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📂 Project Structure

```
CivicTracker/
├── app/                  # Main application module
│   ├── src/main/java/    # Kotlin source (UI, ViewModels, data layer)
│   └── src/main/res/     # Resources (layouts, drawables, strings)
├── gradle/               # Gradle wrapper files
├── build.gradle.kts      # Project-level build config
└── settings.gradle.kts   # Module settings
```

---

## 🗺️ Roadmap

- [ ] Role-based access control for the Officer Dashboard
- [ ] Real-time resolution-time analytics on the Public Scorecard
- [ ] "My Issues" screen for citizens to track their own submitted reports
- [ ] Push notifications on status changes

---

## 🤝 Contributors

- [anjalikhonde](https://github.com/anjalikhonde)
- [renukaSandbhor01](https://github.com/renukaSandbhor01)

---

## 📄 License

This project currently has no license specified. Add one (e.g. [MIT](https://choosealicense.com/licenses/mit/)) if you'd like others to use or contribute to this code.
