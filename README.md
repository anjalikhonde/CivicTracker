# 🏙️ CivicTracker

**CivicTracker** is a native Android app that makes it simple for citizens to report the everyday civic problems they run into — potholes, broken streetlights, garbage pileups, water leaks, and more — and track how those reports get resolved.

Built with Kotlin and Jetpack Compose, backed by Supabase and Firebase Auth, with an AI-assisted issue check powered by Gemini.

---

## ✨ Features

- **Report an Issue** — Capture a photo (camera or gallery), add a description, and pin the location of a civic problem in a few taps.
- **AI Smart Check** — Uploaded reports are automatically analyzed using Google's Gemini model to help validate and categorize issues before submission.
- **Issue Tracking** — Citizens can follow the status of their reports from submission through resolution.
- **Officer Dashboard** — A dedicated view for civic officials to review, manage, and update the status of reported issues.
- **Public Scorecard** — A transparency view showing aggregate stats on how issues in the community are being resolved.
- **Secure Auth** — User accounts and sessions are handled via Firebase Authentication.

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

## 📱 Screenshots

> _Add screenshots of the Login, Home, Report Issue, and Officer Dashboard screens here._

|  |  |  |
|---|---|---|
| ![Login](docs/screenshots/login.png) | ![Home](docs/screenshots/home.png) | ![Report Issue](docs/screenshots/report.png) |

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
