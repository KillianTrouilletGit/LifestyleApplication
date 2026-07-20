package com.example.personallevelingsystem.util

import android.content.Context
import android.content.SharedPreferences
import com.example.personallevelingsystem.model.MissionCategory

/**
 * Single SharedPreferences gateway for everything mission-adjacent that doesn't
 * deserve a Room table: streaks, category XP, achievement flags, reminder settings,
 * the "weight last logged" timestamp, etc.
 *
 * All keys are namespaced to keep the file tidy.
 */
class MissionPrefs private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ---- Streaks --------------------------------------------------------------

    fun getStreak(missionId: String): Int = prefs.getInt(streakKey(missionId), 0)

    fun setStreak(missionId: String, value: Int) {
        prefs.edit().putInt(streakKey(missionId), value).apply()
    }

    /** Last calendar day (yyyyMMdd as Int) the mission was completed. */
    fun getLastCompletedDay(missionId: String): Int = prefs.getInt(lastDayKey(missionId), 0)

    fun setLastCompletedDay(missionId: String, dayKey: Int) {
        prefs.edit().putInt(lastDayKey(missionId), dayKey).apply()
    }

    // ---- Category XP ----------------------------------------------------------

    fun getCategoryXp(category: MissionCategory): Int = prefs.getInt(categoryXpKey(category), 0)

    fun addCategoryXp(category: MissionCategory, amount: Int) {
        prefs.edit().putInt(categoryXpKey(category), getCategoryXp(category) + amount).apply()
    }

    fun allCategoryXp(): Map<MissionCategory, Int> =
        MissionCategory.values().associateWith { getCategoryXp(it) }

    // ---- Weight timestamp -----------------------------------------------------

    fun getWeightLoggedAt(): Long = prefs.getLong(KEY_WEIGHT_LOGGED_AT, 0L)

    fun setWeightLoggedAt(timestampMs: Long) {
        prefs.edit().putLong(KEY_WEIGHT_LOGGED_AT, timestampMs).apply()
    }

    // ---- Reminder settings ----------------------------------------------------

    fun areNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_MASTER, true)
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_MASTER, enabled).apply()
    }

    fun areQuietHoursEnabled(): Boolean = prefs.getBoolean(KEY_QUIET_ENABLED, true)
    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUIET_ENABLED, enabled).apply()
    }

    fun getQuietStartHour(): Int = prefs.getInt(KEY_QUIET_START, 22)
    fun setQuietStartHour(hour: Int) {
        prefs.edit().putInt(KEY_QUIET_START, hour.coerceIn(0, 23)).apply()
    }

    fun getQuietEndHour(): Int = prefs.getInt(KEY_QUIET_END, 7)
    fun setQuietEndHour(hour: Int) {
        prefs.edit().putInt(KEY_QUIET_END, hour.coerceIn(0, 23)).apply()
    }

    fun isBehindOnlyMode(): Boolean = prefs.getBoolean(KEY_BEHIND_ONLY, false)
    fun setBehindOnlyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BEHIND_ONLY, enabled).apply()
    }

    fun isSlotEnabled(slot: String): Boolean = prefs.getBoolean(slotKey(slot), true)
    fun setSlotEnabled(slot: String, enabled: Boolean) {
        prefs.edit().putBoolean(slotKey(slot), enabled).apply()
    }

    // ---- Achievement flags ----------------------------------------------------

    fun hasAchievement(id: String): Boolean = prefs.getBoolean(achievementKey(id), false)
    fun markAchievement(id: String) {
        prefs.edit().putBoolean(achievementKey(id), true).apply()
    }

    // ---- Snooze ---------------------------------------------------------------

    fun getSnoozedUntil(slot: String): Long = prefs.getLong(snoozeKey(slot), 0L)
    fun snoozeSlot(slot: String, untilMs: Long) {
        prefs.edit().putLong(snoozeKey(slot), untilMs).apply()
    }

    // ---- Keys -----------------------------------------------------------------

    private fun streakKey(id: String) = "streak::$id"
    private fun lastDayKey(id: String) = "lastday::$id"
    private fun categoryXpKey(c: MissionCategory) = "catxp::${c.name}"
    private fun slotKey(slot: String) = "slot::$slot"
    private fun achievementKey(id: String) = "ach::$id"
    private fun snoozeKey(slot: String) = "snooze::$slot"

    companion object {
        private const val FILE = "mission_prefs_v2"
        private const val KEY_WEIGHT_LOGGED_AT = "weight_logged_at"
        private const val KEY_NOTIF_MASTER = "notif_master"
        private const val KEY_QUIET_ENABLED = "quiet_enabled"
        private const val KEY_QUIET_START = "quiet_start"
        private const val KEY_QUIET_END = "quiet_end"
        private const val KEY_BEHIND_ONLY = "behind_only"

        @Volatile private var INSTANCE: MissionPrefs? = null

        fun get(context: Context): MissionPrefs = INSTANCE ?: synchronized(this) {
            INSTANCE ?: MissionPrefs(context).also { INSTANCE = it }
        }
    }
}

/** yyyyMMdd as Int — collation-friendly day key. */
fun todayDayKey(): Int {
    val c = java.util.Calendar.getInstance()
    return c.get(java.util.Calendar.YEAR) * 10000 +
            (c.get(java.util.Calendar.MONTH) + 1) * 100 +
            c.get(java.util.Calendar.DAY_OF_MONTH)
}

fun yesterdayDayKey(): Int {
    val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH, -1) }
    return c.get(java.util.Calendar.YEAR) * 10000 +
            (c.get(java.util.Calendar.MONTH) + 1) * 100 +
            c.get(java.util.Calendar.DAY_OF_MONTH)
}

/**
 * Calendar days between two yyyyMMdd day keys (0 = same day, 1 = consecutive days).
 * Returns Int.MAX_VALUE when either key is unset so callers treat it as "no history".
 */
fun daysBetweenDayKeys(from: Int, to: Int): Int {
    if (from <= 0 || to <= 0) return Int.MAX_VALUE
    val cal = java.util.Calendar.getInstance()
    cal.clear()
    cal.set(from / 10000, (from / 100) % 100 - 1, from % 100)
    val a = cal.timeInMillis
    cal.clear()
    cal.set(to / 10000, (to / 100) % 100 - 1, to % 100)
    val b = cal.timeInMillis
    return ((b - a) / 86_400_000L).toInt()
}

/** ISO week key: yyyy * 100 + weekOfYear. */
fun thisWeekKey(): Int {
    val c = java.util.Calendar.getInstance()
    return c.get(java.util.Calendar.YEAR) * 100 + c.get(java.util.Calendar.WEEK_OF_YEAR)
}
