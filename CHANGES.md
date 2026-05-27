# Missions Upgrade + Notification Reminders

Summary of all changes made on branch `claude/hardcore-brattain-ffbbf9`.

> **Phase 2 update** — auto-completion, streaks with multiplier, inline notification
> actions, quiet hours, achievements, briefings, settings screen, and a Top-Ops
> card on the home dashboard. See "Phase 2" section below for the additions
> layered on top of the original missions/notifications rewrite.

---

## 1. Mission Model — richer, themed, categorized

**File:** `app/src/main/java/com/example/personallevelingsystem/model/Mission.kt`

The old `Mission` had only `description`. It now carries:

```kotlin
data class Mission(
    val id: String,
    val title: String,                                  // NEW — short HUD-style name
    val description: String,                            // rewritten, action-oriented
    val type: MissionType,                              // DAILY / WEEKLY (unchanged)
    val category: MissionCategory,                      // NEW — drives accent color
    val isCompleted: Boolean,
    val reward: Int,
    val difficulty: MissionDifficulty = MissionDifficulty.NORMAL  // NEW
)

enum class MissionCategory { BODY, MIND, NUTRITION, RECOVERY, DISCIPLINE, PROGRESS }
enum class MissionDifficulty { NORMAL, HARD, ELITE }
```

---

## 2. Mission Copy Rewrite

**File:** `app/src/main/java/com/example/personallevelingsystem/repository/MissionRepository.kt`

### Daily missions (10)

| ID | Old | New title | New description |
|---|---|---|---|
| daily_flex | "Complete a 15-minutes flexibility training" | **Mobility Protocol** | Run a 15-minute flexibility session — open hips, spine, shoulders. |
| daily_water | "Drink the correct amount of water" | **Hydration Quota** | Drink your daily water target (≈ 35 ml per kg of body weight). |
| daily_learn | "Practice micro learning for 30 minutes" | **Knowledge Drip** | Deep-focus learning for 30 minutes — no phone, no tabs. |
| daily_meditate | "Meditate for 10 minutes" | **Mind Reset** | Meditate or breathwork for 10 minutes — eyes closed, no input. |
| daily_hygiene | "Make sure to have proper hygiene" | **Maintenance Pass** | Full hygiene cycle: shower, teeth, skin, nails, hair. |
| daily_sleep | "Sleep over 7 hours" | **Recovery Cycle** | Log 7+ hours of sleep — your stats regenerate while you're offline. |
| daily_nutrition | "Be sure to have a proper nutrition" | **Fuel Balance** | Hit a balanced macro split today — protein, complex carbs, healthy fats. |
| daily_planning | "Respect today's planning" | **Execute the Plan** | Clear today's planning blocks — no skipped, no postponed. |
| daily_planning_2 | "Fine tune tomorrow's planning" | **Tomorrow's Briefing** | Lock in tomorrow's schedule before bed — 3 priorities minimum. |
| daily_appearance | "Work on external appearance" | **Operator Standard** | Groom and dress with intent — like you might meet anyone today. |

### Weekly missions (7)

| ID | Old | New title | New description | Difficulty |
|---|---|---|---|---|
| weekly_workout | "Complete the workout program" | **Training Arc Complete** | Finish every session of this week's program — no missed sets. | ELITE |
| weekly_planning | "Create next week planning" | **Next-Week Strategy** | Build out next week's planning — workouts, meals, deep-work blocks. | HARD |
| weekly_endurance | "Run 10 kilometers" | **Distance Run** | Cover 10 km of endurance work — outdoor preferred. | HARD |
| weekly_cook | "Cook a new recipe" | **New Recipe Unlocked** | Cook a meal you've never made — expand the menu rotation. | NORMAL |
| weekly_clean | "Clean your living space" | **Base Reset** | Deep-clean your living space — surfaces, floors, laundry, dishes. | NORMAL |
| weekly_report | "Check the progress of the week" | **Weekly Debrief** | Review your performance graphs — note wins, gaps, next focus. | NORMAL |
| weekly_weigh | "Register new weight" | **Body Stat Check** | Log this week's weight — same time, same conditions for accuracy. | NORMAL |

Two new helpers were added: `getIncompleteDailyMissions()` / `getIncompleteWeeklyMissions()` (used by the reminder workers below).

---

## 3. Mission List UI Refresh

**File:** `app/src/main/java/com/example/personallevelingsystem/ui/compose/screens/MissionsListScreen.kt`

- Each mission now renders as a bordered card with:
  - **Vertical accent bar** — color-coded by category (magenta=BODY, violet=MIND, green=NUTRITION, cyan=RECOVERY, orange=DISCIPLINE, white=PROGRESS).
  - **Title** uppercase + bold.
  - **Difficulty pill** (HARD / ELITE) for non-normal weekly ops.
  - **Description** in dimmed hologram-text.
  - **`+XP` badge** in neon cyan.
  - **Gradient checkbox** (28dp).
- Completed missions: telemetry-green border, faded title/description.
- Section headers (`DAILY OPS`, `WEEKLY OPS`) now show progress `done / total`.

---

## 4. Notification Channels

**File:** `app/src/main/java/com/example/personallevelingsystem/util/NotificationUtils.kt`

Added three new channels and helper builders:

| Channel ID | Purpose | Importance |
|---|---|---|
| `mission_reminder_channel` | Mid-day / evening nudges | DEFAULT |
| `logging_reminder_channel` | Prompts when expected data is missing | DEFAULT |
| `last_call_channel` | Urgent, before reset | HIGH |

New builder APIs:
- `showMissionReminder(...)` — multi-line `BigTextStyle` listing top 5 incomplete missions with their XP rewards.
- `showLoggingReminder(...)` — supports optional `deeplink_route` extra (e.g. `"water"`, `"sleep"`, `"planning"`) so future code can route taps into the right screen.
- `cancelReminder(id)` — clears a posted reminder when the underlying state changes.

Stable notification IDs reserved: `3001-3004` for mission reminders, `4001-4005` for logging reminders.

---

## 5. Reminder Workers

### `MissionReminderWorker` — `app/src/main/java/com/example/personallevelingsystem/worker/MissionReminderWorker.kt`

Reads its slot from input data and posts the right notification (silently skipping if there's nothing to remind):

| Slot | Wall-clock | Behavior |
|---|---|---|
| `SLOT_MIDDAY` | 12:30 | "Mid-Day Check-In — N daily ops still pending." |
| `SLOT_EVENING` | 18:00 | "Window Closing — N missions left before the day ends." |
| `SLOT_LAST_CALL_DAILY` | 21:30 | **Urgent** — "LAST CALL — X XP at stake. N ops still unlocked." |
| `SLOT_LAST_CALL_WEEKLY` | Sun 19:00 | **Urgent** — "WEEKLY RESET TONIGHT — X XP burning." |

### `LoggingReminderWorker` — `app/src/main/java/com/example/personallevelingsystem/worker/LoggingReminderWorker.kt`

Each slot checks the relevant DAO and only fires when data is genuinely missing:

| Slot | Wall-clock | Fires when… |
|---|---|---|
| `SLOT_SLEEP_MORNING` | 09:00 | No sleep entry recorded today → "Log Last Night's Recovery." |
| `SLOT_WATER_MORNING` | 10:00 | Zero water entries → links to water screen, shows computed target (`weight × 35 ml` or 2500 ml default). |
| `SLOT_NUTRITION_LUNCH` | 13:30 | Zero meals → "Already past lunch and the nutrition log is empty." |
| `SLOT_WATER_AFTERNOON` | 15:00 | Under 50% of water target → "You're at X / Y ml. About Z ml left." |
| `SLOT_NUTRITION_DINNER` | 20:30 | Fewer than 2 meals logged → "Dinner check-in." |
| `SLOT_PLANNING_EVENING` | 21:00 | Always → "Tomorrow's Briefing — set 3 priorities and lock the schedule." |
| `SLOT_WEIGHT_SUNDAY` | Sun 10:00 | Always → "Weekly Weigh-In — same time, same conditions." |

Each reminder embeds a deeplink route hint (`water`, `sleep`, `nutrition`, `planning`, `profile`) for future router integration.

---

## 6. Scheduling

**File:** `app/src/main/java/com/example/personallevelingsystem/scheduler/ReminderScheduler.kt`

- Single entry point `scheduleAll()` enqueues 11 unique periodic `WorkRequest`s (10 daily + 1 weekly) via `WorkManager`.
- Each request uses `ExistingPeriodicWorkPolicy.UPDATE`, so calling `scheduleAll()` again from app start is idempotent (initial delays recomputed to land on the right wall-clock).
- Initial delay helpers (`initialDelayMinutesToTimeOfDay`, `initialDelayMinutesToDayAndTime`) handle the rollover edge case (target time today already passed → push to tomorrow / next week).

---

## 7. Wiring

**File:** `app/src/main/java/com/example/personallevelingsystem/MyApplication.kt`

```kotlin
override fun onCreate() {
    super.onCreate()
    NotificationUtils.createNotificationChannel(this)
    ReminderScheduler(this).scheduleAll()   // NEW
}
```

`POST_NOTIFICATIONS` permission was already declared in `AndroidManifest.xml`, so no manifest changes were needed.

---

## 8. Build

`:app:assembleDebug` — **BUILD SUCCESSFUL**. Only pre-existing warnings (unused params in `MainScreen`, `Theme`, etc.); no new errors or warnings introduced by these changes.

Environment note: a `local.properties` was generated pointing to the Android SDK at `C:\Users\ktrou\AppData\Local\Android\Sdk`, and the build must run with `JAVA_HOME` pointing at JDK 17 (Android Studio's bundled JBR works) because Kotlin 1.8.10 cannot target JVM 21.

---

## Files Touched (Phase 1)

```
 modified:   app/src/main/java/com/example/personallevelingsystem/MyApplication.kt
 modified:   app/src/main/java/com/example/personallevelingsystem/model/Mission.kt
 modified:   app/src/main/java/com/example/personallevelingsystem/repository/MissionRepository.kt
 modified:   app/src/main/java/com/example/personallevelingsystem/ui/compose/screens/MissionsListScreen.kt
 modified:   app/src/main/java/com/example/personallevelingsystem/util/NotificationUtils.kt
 added:      app/src/main/java/com/example/personallevelingsystem/scheduler/ReminderScheduler.kt
 added:      app/src/main/java/com/example/personallevelingsystem/worker/MissionReminderWorker.kt
 added:      app/src/main/java/com/example/personallevelingsystem/worker/LoggingReminderWorker.kt
 added:      local.properties                  (build-environment only; gitignored)
 added:      CHANGES.md                        (this document)
```

---

# Phase 2 — Efficiency & UX overhaul

Layered on top of Phase 1 after the UI revamp (crimson glassmorphism palette) was merged.

## 9. Auto-completion from tracked data

The `Mission` model gained a `requirement: MissionRequirement` field — a sealed class describing how a mission can be auto-completed from underlying Room data:

```kotlin
sealed class MissionRequirement {
    object Manual : MissionRequirement()
    object WaterDailyTarget : MissionRequirement()
    data class SleepHoursAtLeast(val hours: Float) : MissionRequirement()
    data class FlexibilityMinutes(val minutes: Int) : MissionRequirement()
    data class NutritionBalance(val threshold: Float) : MissionRequirement()
    data class EnduranceWeeklyKm(val km: Float) : MissionRequirement()
    object TrainingSessionLoggedToday : MissionRequirement()
    object WeightLoggedThisWeek : MissionRequirement()
}
```

Wired:
- `daily_water` → 35 ml/kg target (defaults to 2500 ml if no weight set)
- `daily_sleep` → 7+ hours logged today
- `daily_flex` → 15+ min flexibility entry today
- `daily_nutrition` → average daily balance index ≥ 0.7
- `weekly_endurance` → 10 km cumulative this week
- `weekly_weigh` → user weight changed this calendar week

**Service:** `service/MissionAutoCompleter.kt` (new) computes per-mission `MissionProgress(current, target, unit)` and runs a `sweep()` that ticks any newly-met mission.

**Triggers:** every save in `HealthViewModel` (water/sleep/meal), every save in `TrainingViewModel` (flex/endurance), every mission reminder worker tick. `UserRepository.updateUser()` stamps `weightLoggedAt` in prefs.

## 10. Streaks + XP multiplier

- Per-mission streak persisted in `MissionPrefs` (new) by tracking `lastCompletedDay` as a `yyyyMMdd` int key.
- Multiplier: `+10% XP per 7-day tier`, capped at `+50%`.
- Streak displayed inline on each mission row as a 🔥{N} pill; XP badge shows the multiplied amount with `×1.2` etc.
- Streaks break on missed daily; weekly missions always increment when completed.

## 11. Per-category XP / Specialization

`MissionPrefs.addCategoryXp(category, amount)` accumulates per `MissionCategory`. Surfaced as a horizontal bar chart card at the top of `MissionsListScreen` ("SPECIALIZATION · 1,240 XP TOTAL") — each category gets a tinted progress bar with the raw XP number.

## 12. Inline notification action buttons

`receiver/NotificationActionReceiver.kt` (new) handles four broadcast actions:

| Action | Buttons posted | Result |
|---|---|---|
| `ACTION_LOG_WATER` | `+250 ml`, `+500 ml`, `+750 ml` | inserts `Water` row, runs sweep, dismisses notification |
| `ACTION_LOG_SLEEP` | `7h`, `8h`, `9h` | inserts `Sleep` row, sweep, dismiss |
| `ACTION_COMPLETE_MISSION` | `DONE: {title}` on mission reminders | completes top mission, awards XP w/ multiplier |
| `ACTION_SNOOZE` | `SNOOZE 1H` on every reminder | sets `snoozedUntil` in prefs |

Receiver declared in `AndroidManifest.xml`. PendingIntents wired through `NotificationUtils`.

## 13. Quiet hours + suppression gating

`NotificationUtils.shouldSuppress(slot, urgent)` consults:
- Master notifications switch (`MissionPrefs.areNotificationsEnabled`)
- Per-slot enable flag (`isSlotEnabled`)
- Per-slot snooze (`getSnoozedUntil` > now)
- Quiet hours window (`getQuietStartHour`..`getQuietEndHour`, default 22→07, supports midnight wrap)
- `urgent = true` bypasses quiet hours (last-call notifications still fire at 21:30)

Every reminder builder routes through this gate.

## 14. Smart consolidation: Daily Digest

Evening 18:00 slot replaced its per-slot mission notification with a consolidated **Daily Status** card that lists missions remaining *and* logs missing (`water`, `sleep`, `meals`), via `NotificationUtils.showDailyDigest(...)`. Reduces notification spam when multiple things are pending at the same time.

## 15. Achievement notifications

`NotificationUtils.showAchievement(id, title, body)` on its own channel (`ACHIEVEMENT_CHANNEL_ID`). Fires once via `MissionPrefs.markAchievement(id)` for:
- Streak milestones at 7 / 30 / 100 days (per mission)
- First-ever all-daily clear ("First Full Clear Unlocked")
- Subsequent same-day full clears ("Perfect Day")

Level-up notifications were already implemented; they now respect the suppression gate.

## 16. Morning briefing + Sunday debrief

New `worker/BriefingWorker.kt`:
- **07:30 daily** — `Morning Briefing` rich card: last-night sleep score + today's incomplete mission count + (placeholder for first planning item). Adaptive sleep messaging based on hours logged.
- **Sunday 20:00** — `Weekly Debrief`: total XP earned that week, current level, top + weakest category, count of streaks ≥ 7 days held.

Both posted to the new `BRIEFING_CHANNEL_ID`.

## 17. Reminder settings screen

`ui/compose/screens/ReminderSettingsScreen.kt` (new) — replaces the old "settings" route. Exposes:
- **Master switch** for notifications
- **Quiet hours** toggle + start/end hour steppers
- **Behind-only mode** (skip mid-day pings unless 3+ ops open)
- **Per-slot toggles** for all 13 reminder slots (mission, logging, briefing)

Uses the crimson glassmorphism palette and `JuicyButton` for consistency.

## 18. Mission detail bottom sheet + deeplinks

Tapping any mission row in `MissionsListScreen` opens a `ModalBottomSheet` with:
- Title + description
- Stat chips: CATEGORY · STREAK · XP
- Inline `MissionProgress` bar where applicable
- TIP (per-mission strategy hint added to each mission)
- "OPEN {ROUTE}" deeplink button to jump into the relevant screen (water/sleep/nutrition/planning/training/profile)
- "MARK COMPLETE" if not done

Notification taps that include a `deeplink_route` extra are honored in `MainActivity` after the splash transitions to the main screen.

## 19. Top Ops card on MainScreen

`ui/compose/components/TopMissionsCard.kt` (new) — surfaces the top 3 incomplete daily missions on the dashboard, above the performance carousel, with `+N XP AVAILABLE` and a one-tap path into the full mission list. Shows "ALL CLEAR · Streaks alive" when nothing's open.

## 20. Inline progress on mission rows

`MissionsListScreen` now reads from `MissionViewModel.missionProgress: LiveData<Map<String, MissionProgress>>` and renders a thin gradient progress bar + `1850 / 2500 ml` line under data-driven missions. The same bar appears in the detail sheet.

---

## Phase 2 Files Touched

```
 modified:   AndroidManifest.xml                                          (+ NotificationActionReceiver)
 modified:   app/src/main/java/.../MainActivity.kt                        (settings route, deeplinks, MissionVM injection)
 modified:   app/src/main/java/.../model/Mission.kt                       (+ requirement, deeplinkRoute, tip)
 modified:   app/src/main/java/.../repository/MissionRepository.kt        (added requirements/deeplinks/tips, findById)
 modified:   app/src/main/java/.../repository/UserRepository.kt           (weight-logged-at stamp)
 modified:   app/src/main/java/.../util/NotificationUtils.kt              (channels, gate, digest, briefings, achievements, action btns)
 modified:   app/src/main/java/.../scheduler/ReminderScheduler.kt         (briefing workers)
 modified:   app/src/main/java/.../viewmodel/MissionViewModel.kt          (progress + streaks + category XP, sweep-on-refresh)
 modified:   app/src/main/java/.../viewmodel/HealthViewModel.kt           (sweep on save)
 modified:   app/src/main/java/.../viewmodel/TrainingViewModel.kt         (sweep on save)
 modified:   app/src/main/java/.../worker/MissionReminderWorker.kt        (consolidation, behind-only, slot gating)
 modified:   app/src/main/java/.../worker/LoggingReminderWorker.kt        (water/sleep inline actions)
 modified:   app/src/main/java/.../ui/compose/screens/MainScreen.kt       (TopMissionsCard, MissionVM param)
 modified:   app/src/main/java/.../ui/compose/screens/MissionsListScreen.kt (progress + streak + detail sheet + specialization card)
 added:      app/src/main/java/.../util/MissionPrefs.kt                   (streaks, category XP, settings, achievements)
 added:      app/src/main/java/.../service/MissionAutoCompleter.kt        (progress + sweep + streak/XP awards)
 added:      app/src/main/java/.../receiver/NotificationActionReceiver.kt (inline action handler)
 added:      app/src/main/java/.../worker/BriefingWorker.kt               (morning briefing + Sunday debrief)
 added:      app/src/main/java/.../ui/compose/screens/ReminderSettingsScreen.kt
 added:      app/src/main/java/.../ui/compose/components/TopMissionsCard.kt
```

## Phase 2 Build

`:app:compileDebugKotlin` ✅
`:app:assembleDebug` ✅ — clean. Only pre-existing warnings (unused `healthViewModel` param now slightly misleading since `MainScreen` uses missionVM instead, but compile-clean).
