# Yami_Films — Play Console: answers & checklist

Prepared 31 Oct 2026. Requirements change — re-check the wording inside Play Console when you fill each form.

## 0. Before anything — money and identity

- [ ] Google Play Console developer account: play.google.com/console — **one-time US$25 fee**, paid by card.
- [ ] Identity verification with an official ID document (Google checks it — can take a few days).
- [ ] Personal account type (you publish as an individual).

## 1. Build changes already made in this project

- [x] `targetSdk` and `compileSdk` raised to **36** (Android 16) — required for new apps since 31 Aug 2026.
- [x] App has no native libraries (pure Kotlin/Android), so the 16 KB page-size rule is not an issue.
- [ ] Build a **signed release AAB** (Play does NOT accept our debug APK for new apps):
      Android Studio → **Build → Generate Signed App Bundle / APK(s)** → **Android App Bundle** →
      **Create new…** keystore.
      ⚠️ The keystore file + its passwords are your app's identity forever. Save the file in
      TWO places (PC + USB/Drive private). Never put it on GitHub, never send it to anyone.
      If you lose it, updating your own app becomes very hard.
- [ ] Note the generated file: `app/build/outputs/bundle/release/app-release.aab`.

## 2. Create the app in Play Console

- [ ] Create app → name: **Yami_Films – Movie Recommendations** → App (not game) → Free.
- [ ] Store listing: paste the texts from `play-store/store-listing.md`.
- [ ] Graphics you must prepare:
  - App icon 512×512 PNG (the Yami_Films logo — can be made later in Android Studio's Image Asset tool from any square image).
  - Feature graphic 1024×500 PNG/JPG.
  - At least 2 phone screenshots — easy: run the app in the emulator, use the emulator's camera 📷 button, or screenshot it on your phone.

## 3. "App content" declarations — the honest answers for Yami_Films

- **Privacy policy**: paste the public URL of `play-store/privacy-policy.md` (see the hosting note at the bottom of that file).
- **Data safety** — declare exactly this (it matches the code):
  - Data collected by the developer: **No data collected**.
  - Data shared with third parties: the app sends the user's search query to TMDB to answer it — in Play's form this is data the user provides that is sent to a third-party service at their request; answer the form's questions so that: no data is sold, no data is used for advertising or analytics.
  - Data encrypted in transit: **Yes** (TMDB is HTTPS).
  - Account creation: **No account**.
  - Data deletion: no developer-held data exists to delete.
- **Content rating (IARC questionnaire)**: answer honestly — the app displays movie information and posters from TMDB; no violence/sexual content is produced by the app itself; no user-generated content, no chats, no purchases. Expect a low rating (Everyone / PEGI 3-ish) but let the questionnaire decide.
- **Target audience**: 13+ is the simple, safe choice for a general movie app (avoids the extra "Designed for Families" rules for child-directed apps).
- **Ads declaration**: **No ads**.
- **Government / news / health declarations**: No to all — not applicable.
- **TMDB attribution**: keep the "This product uses the TMDB API but is not endorsed or certified by TMDB" line in the store description (already in the listing text). TMDB's terms ask API users to attribute them.

## 4. The closed test — the step that takes the time

New personal accounts (created after 13 Nov 2023) must run a **closed test** before production:

- [ ] Create a **Closed test track** in Play Console → Testing → Closed testing.
- [ ] Upload the signed **AAB**.
- [ ] Add **at least 12 testers** by email — this is where your friends come in: ask 12+ friends for the Gmail address they use on their Android phone, add them as testers, send them the opt-in link Play Console gives you.
- [ ] Testers must stay opted in for **14 consecutive days** (and actually install/open the app).
- [ ] After 14 days, apply for **production access** in Play Console and answer its questions about your test (what you tested, feedback received, changes made).
- [ ] Then create the **Production release** with the same AAB (or a newer build) and submit for Google review.

Realistic total timeline: **about 3–6 weeks** from account creation to a public listing.

## 5. The public-version API key (decide before the closed test)

Today each user pastes their own TMDB key. That is fine for you and your friends, but a Play Store user will not do that.
Before the closed test, build the test/release version with YOUR key baked in privately:
`local.properties` (never committed) containing `tmdb.api.key=YOUR_KEY`, then build the AAB — the app reads that key automatically.
⚠️ A key inside an app can be extracted by a determined person. For a small personal project this is the standard approach; TMDB keys are free and rate-limited by TMDB. Never publish the key itself in the repo, the store listing, or a screenshot.

## 6. After launch

- [ ] Watch Play Console → Reviews and Android vitals.
- [ ] Every update: raise `versionCode` by 1, build a new signed AAB with the SAME keystore.
