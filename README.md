# 🎬 Yami_Films

A beginner-friendly **Android app** that recommends movies similar to one you already love.

Type a movie title, tap the right result, and **Yami_Films** shows you the 5 most similar movies — with their genre, year, rating and a **similarity score** computed by the app itself.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-purple)
![TMDB](https://img.shields.io/badge/Data-TMDB%20API-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Features

- 📱 A real **Android app** (Kotlin) — install it on your phone, no browser needed
- 🌐 **Online mode**: search almost any movie through the free [TMDB](https://www.themoviedb.org/) API, with posters, ratings and overviews
- 🧠 **Its own recommender**: candidates are ranked *on the phone* by TF-IDF + cosine similarity — not just copied from TMDB's ordering
- 📴 **Offline fallback**: a built-in list of 56 well-known movies, so the app still works with no key and no connection
- 🔑 Your TMDB key is pasted **inside the app** and stays only on your phone — no key in the code, nothing to recompile
- ♥ **My List**: save movies on the phone and get recommendations from them later — everything stored locally
- 🌍 **3 languages** (English, Français, العربية) and a **dark / light appearance**, both in Settings and both remembered
- 🎨 Netflix-style red-on-black design with a bottom navigation bar (Home, Search, My List, Settings) — every button works

## 🧠 How It Works

```
Online:  Search title ──► TMDB API ──► movie details + keywords
                                   └─► popular / top-rated pool
Offline: built-in movies.csv ──────┘
                     │
                     ▼
        TF-IDF (genres + overview + keywords → numbers)
                     │
                     ▼
        Cosine similarity against every candidate
                     │
                     ▼
        Top 5 similar movies + similarity %
```

1. **TF-IDF** turns each movie's text into a vector. Words that are important in one movie but rare in the others weigh more.
2. **Cosine similarity** compares the chosen movie's vector with every candidate's vector (1.0 = same themes, 0.0 = nothing in common).
3. The best 5, excluding the chosen movie itself, are displayed.

### Verified example (offline dataset, `tools/verify_recommender.py`)

```
Loaded 56 movies
Inception        → Tenet 22.06%, Guardians of the Galaxy 16.08%, The Matrix 14.44% …
The Hangover     → Superbad 39.21%, Step Brothers 15.19%, Knives Out 15.05% …
All checks passed ✅
```

Scores look modest because the offline starter dataset uses short keyword lists; with TMDB's full overviews and keywords, the online ranking has much richer text to compare.

## 🔑 Get your free TMDB key (needed for online mode)

1. Go to [themoviedb.org](https://www.themoviedb.org/) and create a free account.
2. Open **your profile → Settings → API** and copy your **API Key (v3 auth)**.
3. In the app, tap **⚙️ TMDB key**, paste the key, tap **Save**.
4. Done — search any movie. The key never leaves your phone.

*(Build-time alternative: put `tmdb.api.key=YOUR_KEY` in `local.properties`. That file is git-ignored — never commit a real key.)*

## 🚀 Build the APK in Android Studio (Windows)

1. Install [Android Studio](https://developer.android.com/studio) (free).
2. Download this project, unzip it, and in Android Studio choose **Open** → select the `yami-films` folder.
3. Wait for **Gradle sync** to finish (first time downloads what it needs).
4. Menu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. When it finishes, click **locate** — the file is `app/build/outputs/apk/debug/app-debug.apk`.

## 📲 Install the APK on your Android phone

1. Copy `app-debug.apk` to your phone (USB cable, WhatsApp to yourself, Drive…).
2. On the phone, tap the file.
3. If Android asks, allow **Install unknown apps** for your file manager — this is normal for an APK installed outside the Play Store.
4. Tap **Install**, then open **Yami_Films** from your app list. 🎬

You can also run it without a phone: in Android Studio, start an **emulator** and press the green ▶ Run button.

## ✅ Verify the recommendation logic (no Android needed)

```bash
python3 tools/verify_recommender.py
```

This runs the same TF-IDF + cosine-similarity algorithm as the Kotlin code on the same dataset and prints real recommendations. Kotlin unit tests live in `app/src/test/java/com/yami/films/RecommenderTest.kt`.

## 📁 Project structure

```
yami-films/
├── app/src/main/java/com/yami/films/
│   ├── MainActivity.kt            # the screen: search, tap, recommend
│   ├── data/Movie.kt              # one movie
│   ├── data/TmdbClient.kt         # TMDB search, details, keywords, pool
│   ├── data/OfflineMovies.kt      # built-in list loader (fallback)
│   └── recommender/Recommender.kt # TF-IDF + cosine similarity
├── app/src/main/assets/movies.csv # 56 movies, offline dataset
├── app/src/test/…/RecommenderTest.kt
├── tools/verify_recommender.py    # Python check of the same algorithm
└── README.md
```

## ⚠️ Honest limitations

- Content-based only: it compares movie *content*, it does not learn from millions of users like Netflix (that is collaborative filtering — a good next step).
- The offline dataset is small (56 movies) on purpose: it is a starter you can read and extend.
- Online mode needs internet and your own free TMDB key.

## 📌 About

Built as a beginner AI/Android project to support an application to the **CaCtus** summer programme — to show hands-on machine-learning work (TF-IDF, cosine similarity) inside a real, installable mobile app.
