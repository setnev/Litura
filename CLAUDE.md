# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Litura is a "Duolingo for Novels" Android app that drives competitive reading through bites (short text segments) + comprehension questions + XP + health + streaks. Users read bites, answer 2 randomized multiple-choice questions, earn XP, and manage a Health meter. The goal is an acquisition-grade Alpha with a scalable content package system.

**Full build specification:** `files/agents.md` — this is the authoritative instruction sheet. Always consult it for detailed requirements.

## Non-Negotiables

- **Kotlin + Jetpack Compose + Material 3**
- **MVVM + Repository + DataStore + Room** (offline-first)
- **JSON-driven content:** "Book packages" in `assets/books/{bookId}/` for Alpha; cloud later
- **Separation of concerns:** Content != Game Engine != User Progress
- **Feature flags:** Everything tunable must be flaggable
- **No hardcoding** of book-specific logic in UI or game logic

## Tech Stack

- **Language:** Kotlin (Java 11 source/target)
- **Platform:** Android (minSdk 29, targetSdk 36, compileSdk 36)
- **Build:** Gradle 9.1.0, Kotlin DSL, AGP 9.0.0
- **UI:** Jetpack Compose, Material 3
- **DI:** Hilt
- **Serialization:** kotlinx.serialization
- **Persistence:** Room (books, bites, questions, purchases, progress, telemetry), DataStore (settings, feature flags, session state)
- **Testing:** JUnit 4 (unit), Espresso 3.7 (instrumented)
- **Dependency versions:** `gradle/libs.versions.toml`

## Build & Test Commands

```bash
# Build
./gradlew build                    # Full build
./gradlew assembleDebug            # Debug APK
./gradlew assembleRelease          # Release APK

# Test
./gradlew test                     # Unit tests
./gradlew connectedAndroidTest     # Instrumented tests (requires device/emulator)

# Single test class
./gradlew test --tests "com.litura.app.ExampleUnitTest"

# Clean
./gradlew clean
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture

### Three Separable Domains

1. **Book Package Content** — book metadata, bites, questions, badge assets. Lives in `assets/books/{bookId}/` as JSON files. Parsed with kotlinx.serialization.
2. **Game Engine Config** — XP rules, health system, question rules, review text limits, competition settings, feature flags. Single `assets/game_engine.json`.
3. **User Progress** — per-user reading progress, KPIs, streaks, competitive stats. Stored in Room + DataStore.

### Module & Package Layout

- Single module: `app/`
- Package: `com.litura.app` under `app/src/main/java/com/litura/app/`
- Resources: Material 3 theming with DayNight support (`values/` and `values-night/`)

### Data Flow

Content JSON (assets) → Room index (via importer on first launch) → Repository → ViewModel → Compose UI

### Navigation

- **Non-reading mode:** Bottom nav with Home / Library / Badges / Skills / Profile. Top bar with greeting + health indicator (battery-style, 10 segments).
- **Reading mode (immersive):** Bottom nav hidden. Top bar shows Quit button, progress bar with chapter label, health indicator. Transition splash shows random "reading fact".

## JSON Schema Reference (`files/`)

These files define the data models. Use them to derive Kotlin data classes and Room entities:

| File | Purpose | Key Fields |
|------|---------|------------|
| `book.json` | Book package manifest | bookId, identity, classification (lexile/difficultyTier), structure (chapters with biteRanges), learningFocus (skills), badges, economics (price/purchaseType), assets |
| `bites.json` | Reading segments | biteId, chapterId, orderIndex, text, estimatedSeconds |
| `questions.json` | Comprehension questions | questionId, biteId, type, difficulty, prompt, choices[], correctChoiceId, explanation |
| `game_engine.json` | Engine configuration | xpRules, healthSystem, questionRules, reviewText, competition, readingSession, difficultyScaling |
| `user_profile.json` | User identity | userId, displayName, avatarId, subscription (tier, entitlements) |
| `reading_progress.json` | Per-book progress | currentBite, completedBites, performance (avgCompetency, avgAttempts), timestamps |
| `competitive_profile.json` | Competitive stats | totalXP, streaks, comparisons (friendsAhead/Behind, rankDelta) |

## Game Engine Rules

- **XP:** 10 correct (1st try), 5 half-credit (2nd try), 0 incorrect
- **Health:** 10 segments, -1 per wrong answer, max -4 per bite, 1 segment recharge per 5 min, hard-stop when empty
- **Questions:** 2 randomly selected per bite from that bite's question bank, max 2 attempts each
- **Visual feedback:** 1st wrong → gray out that choice; 2nd wrong → wrong turns red, correct turns green
- **Review Text:** popup limited to 5/day, reset per session in Alpha
- **End-of-bite recap:** time spent reading, XP earned, competency %

## Competitive System (Alpha)

- Local mock: 5-10 seeded fake friends with deterministic progress curves
- Show "ahead/behind" deltas, rank delta ("+1 today"), no exact rank or global leaderboard
- Comparison metrics: XP, competency, bite completion

## Telemetry (Local for Alpha)

Room table `telemetry_events` capturing: eventType (SESSION_START, BITE_STARTED, QUESTION_ATTEMPT, BITE_COMPLETED, HEALTH_CHANGED, REVIEW_TEXT_OPENED), timestamp, bookId, biteId, questionId, attemptNumber, timeToAnswerMs, usedReviewText, xpAwarded, healthDelta, competencyPercent. Debug viewer in Profile behind dev toggle.

## Purchasing (Scaffold Only)

Model `PurchaseState`: NOT_OWNED / OWNED_NOT_DOWNLOADED / OWNED_DOWNLOADED. Alpha uses fake toggles; structured for future Google Play Billing drop-in.

## Implementation Order

1. Project setup: Compose, M3, Navigation, Hilt DI, Room, DataStore
2. Data models + JSON parsing (kotlinx.serialization)
3. Book package importer: read `assets/books/*` into Room
4. Game Engine service: load config, compute XP/health/competency, emit telemetry
5. Reading flow: bite reader → question screen → end recap
6. Dashboard + library screens
7. Health UI + recharge timer
8. Badges + award condition evaluation
9. Competitive mock + dashboard widgets
10. Polish transitions + reading facts splash
