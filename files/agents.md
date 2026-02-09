# Litura — Alpha Build Instruction Sheet (Android Studio AI / Claude Code / Codex)
**Goal:** Generate an acquisition-grade Alpha of **Litura**, a “Duolingo for Novels” app that drives **competitive reading** through bites + questions + XP + health + streaks, with a scalable content package system.

## 0) Non-Negotiables
- **Android:** Kotlin, Jetpack Compose, Material 3
- **Architecture:** MVVM + Repository + DataStore + Room (offline-first)
- **Content:** JSON-driven “book packages” in `assets/` now; later cloud.
- **Separation of Concerns:** Content ≠ Game Engine ≠ User Progress.
- **Feature flags:** Everything tunable must be flaggable.
- **No hardcoding of book-specific logic** in UI or game logic.

## 1) Product Summary
Litura incentivizes competitive reading. Users read “bites” (short segments), answer 2 randomized multiple-choice questions, earn XP, and manage a **Health** meter (10 segments) that decreases on wrong answers and refills over time. Users compare progress against friends (MVP: local simulated friends).

**Alpha requirements:**
- Fully working core loop end-to-end.
- Clean dashboards/library/reading UI per spec.
- Event logging for behavior analytics (local for Alpha).
- Scalable content ingestion via “Book Package” zip or folder format.

## 2) Screens & UX Requirements
### 2.1 Global Navigation (Not Reading)
Bottom nav: **Home / Library / Badges / Skills / Profile**
Top action/info bar: Greeting + Health indicator (battery style, 10 segments).

### 2.2 Reading Mode (Immersive)
- Hide bottom nav entirely.
- Top bar replaces greeting with:
  - **Quit** button (far left)
  - **Progress bar** with chapter/part/section label above it
  - **Health indicator** far right
- Transition into/out of reading shows a random “reading fact” splash (lightweight modal).

### 2.3 Home (Dashboard)
- Greeting on top
- “Books Currently Reading” card(s), format:
  +---------------------------------------------------+
  | [Book Title]                          CompScore:86%|
  | [Author]                            XP Earned:480XP|
  | Progress (26/114) |------[]----------------| 100%  |
  +---------------------------------------------------+
- Hidden KPI tiles (2x2) available via pull-up from top of bottom nav area.

### 2.4 Library
- Shows local + cloud(store) books with search/filter/sort.
- Search button top-left.
- Filters dropdown (author/genre/reader level/free/paid) right of search.
- Sort (reader level, bite count, price).
- Book card format:
  +-----------------------------------------------+---+---+
  | +[Title]                                       | * | $ |
  | [Author]                                       +---+---|
  | Bites:[Count]                          Price:$00.00   |
  |                     \/                                |
  +-----------------------------------------------+---+---+
- Icons:
  - `*` favorites
  - `$` purchase button (toggle between $, lock, unlock)
  - Title prefix icon:
    - Cloud = not purchased/saved
    - Floppy = purchased & downloaded
    - Unlocked lock = purchased not downloaded
- Expand arrow reveals description + challenged concepts + future social tags.

### 2.5 Badges
- Each book completion yields badge.
- Specialty/hidden badges exist; hidden triggered by conditions.
- Badge images shipped with package.

### 2.6 Skills
- Show skill paths (e.g., Observation, Inference).
- Each book maps to primary/secondary skills.

### 2.7 Profile
- Public/private KPI display (some hidden).
- Lifetime stats and streak.

## 3) Game Mechanics Requirements
### Questions & Scoring
- Each bite → randomly select **2 questions** from that bite’s bank.
- Immediate feedback (“Nice try”, “Not quite”, etc).
- Visual elimination:
  - First wrong: gray out wrong
  - Second wrong: wrong turns red, correct turns green
- XP:
  - Correct on first try: **10 XP**
  - Correct on second try: **5 XP**
  - Incorrect after 2 tries: **0 XP**
- End-of-bite recap: time spent reading, XP earned, competency %

### Text Review (Monetizable)
- “Review Text” popup limited to **5 per day**
- In Alpha: reset after each session to simplify monetization scaffolding.

### Health System (“Battery”)
- 10 segments
- When empty: cannot read further
- Refill: 1 segment / 5 minutes
- Lose 1 segment per incorrect answer
- Max loss per bite: 4 segments (40%)

### Competitive Reading (Alpha MVP)
- Add friend comparison as **local mock** (seeded fake users).
- Show “ahead/behind” and deltas; do not show global leaderboards.
- Show rank delta (e.g., “+1 today”) but hide exact rank initially.

## 4) Data & Schemas (MUST IMPLEMENT)
Implement three separable schemas:
1) **Book Package content** (book metadata, bites, questions, assets)
2) **Game Engine config** (xp, health, review limits, competition)
3) **User Progress** (per-user reading progress, KPIs, streaks, competitive stats)

### 4.1 Book Package Schema (assets-based for Alpha)
Book Package can be a folder in `assets/books/{bookId}/` (Alpha) with:
- `book.json` (manifest/metadata)
- `bites.json`
- `questions.json`
- `cover.jpg`
- `badges/*.jpg`

**book.json minimal fields (Alpha):**
- bookId, version
- identity: title, author, language, year, series(optional)
- classification: genres[], lexile, difficultyTier
- structure: totalBites, chapters[{chapterId,title,biteRange}]
- learningFocus: primarySkills[], secondarySkills[]
- social: topicTags[], interestTags[]
- badges: completionBadge, specialtyBadges[], hiddenBadges[] (conditions as strings)
- economics: price, purchaseType, includedInPlans[]
- assets: coverImage, badgeImages[]
- analytics: estimatedReadTimeMinutes, engagementWeight

### 4.2 Game Engine Config (assets/game_engine.json)
Fields:
- xpRules
- healthSystem (enabled, maxSegments, rechargeMinutes, penalties)
- questionRules (questionsPerBite=2, maxAttempts=2)
- reviewText (enabled, dailyLimit=5, resetPolicy)
- competition (enabled, metrics[], visibility)
- readingSession (lockProgressUntilComplete, exitWarning)
- featureFlags (optional map)

### 4.3 User Progress (Room + DataStore)
Store:
- user profile
- per-book progress (currentBite, completedBites, timestamps)
- performance aggregates (avg competency, avg attempts, avg answer time)
- KPIs (daily/monthly/lifetime)
- streaks
- competition comparisons (mock friend deltas)

## 5) Persistence & Offline-first
- **Room** for: books index, bites, questions, purchases, progress events.
- **DataStore** for: user settings, feature flags overrides, session state.
- Import book packages on first launch and index them.

## 6) Analytics / Event Logging (Local)
Create an event table `telemetry_events` capturing:
- eventType: SESSION_START, BITE_STARTED, QUESTION_ATTEMPT, BITE_COMPLETED, HEALTH_CHANGED, REVIEW_TEXT_OPENED
- timestamp
- bookId, biteId, questionId
- attemptNumber, timeToAnswerMs, usedReviewText
- xpAwarded, healthDelta
- competencyPercent

In Alpha: store locally and show optional debug viewer in Profile (hidden behind dev toggle).

## 7) Purchasing (Scaffold Only)
- Implement a `PurchaseState` model:
  - NOT_OWNED, OWNED_NOT_DOWNLOADED, OWNED_DOWNLOADED
- In Alpha: fake purchases (toggle in UI), but structure code so Google Play Billing can be dropped in later.

## 8) Competitive Mock System (Alpha)
- Seed 5–10 mock friends with deterministic progress curves.
- Compute friend deltas based on:
  - XP today
  - bites completed today
  - competency average
- Show simple “You passed Alex today” and “You’re 1 day behind Sam”.

## 9) Implementation Tasks (Order)
1) Project setup: Compose, M3, Navigation, Hilt DI, Room, DataStore
2) Define data models + JSON parsing (kotlinx.serialization)
3) Build importer: read `assets/books/*` into Room index
4) Implement Game Engine service:
   - load config
   - compute xp, health, competency
   - emit telemetry events
5) Implement reading flow:
   - bite reader
   - question screen
   - end recap
6) Implement dashboard + library states
7) Implement health UI + recharge timer
8) Implement badges + award conditions evaluation
9) Implement competitive mock + dashboard widgets
10) Polish transitions + reading facts

## 10) UI Style Notes
- Clean, Duolingo-like: bold icons, clear progress bars, friendly copy.
- Avoid clutter: hide advanced KPIs behind pull-up.
- Reading mode must be immersive.

## 11) Acceptance Criteria (Alpha)
- User can install and immediately see at least 2 books.
- User can start reading, complete bites, answer questions, earn XP.
- Health decreases and recharges correctly; hard-stop works gracefully.
- Dashboard reflects real progress and competency.
- Library supports search/filter/sort and purchase state icons.
- Badges unlock on completion (at least 1 specialty + 1 hidden test badge).
- Telemetry is recorded locally.
- Competitive comparison shows meaningful deltas vs mock friends.

## 12) Output Requirements for the AI
Generate:
- Full Android Studio project code (Kotlin/Compose)
- Room entities/DAO, repositories, viewmodels
- JSON parsing and importer
- Navigation graph
- UI screens per spec
- Seed content book package example in assets
- A `README.md` with setup, architecture, and how to add books

**App Name:** Litura
**Package Name:** com.litura.app

Build this as an Alpha-ready app with clean code, modular structure, and obvious expansion points.
