# 🏙️ CivicTracker

**CivicTracker** is a native Android app that makes it simple for citizens to report the everyday civic problems they run into — potholes, broken streetlights, garbage pileups, water leaks, and more — and track how those reports get resolved.

Built with Kotlin and Jetpack Compose, backed by Supabase and Firebase Auth, with an AI-assisted issue check powered by Gemini.

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

1. **Login** — _screenshot coming soon_
2. **OTP Verification** — _screenshot coming soon_
3. **Home Page**
   ![Home Page](citizen/03-home.png)
4. **Report Submission — Photo & Title**
   ![Add Photo & Title](citizen/04-report-photo-title.png)
5. **Report Submission — Details & Description**
   ![Report Details](citizen/05-report-details.png)
6. **Public Scoreboard** (after submitting)
   ![Public Scoreboard](citizen/06-scoreboard.png)
7. **Map View**
   ![Map View](citizen/07-map.png)
8. **Back to Public Scoreboard**
   ![Public Scoreboard](citizen/08-scoreboard.png)

### 🧑‍💼 Officer Flow

1. **Login** — _screenshot coming soon_
2. **OTP Verification** — _screenshot coming soon_
3. **Home Page**
   ![Home Page](officer/03-home.png)
4. **Officer Dashboard**
   ![Officer Dashboard](officer/04-dashboard.png)
5. **Profile Page**
   ![Profile Page](officer/05-profile.png)
6. **Back to Officer Dashboard**
   ![Officer Dashboard](officer/06-dashboard.png)
7. **Mark Issue as Resolved**
   ![Resolved Action](officer/07-resolved.png)
8. **Home Page**
   ![Home Page](officer/08-home.png)
9. **Public Scoreboard**
   ![Public Scoreboard](officer/09-scoreboard.png)
10. **Scoreboard Result (100%)**
    ![Scoreboard Result](officer/10-scoreboard-result.png)

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
