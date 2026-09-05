package com.example.personallevelingsystem.util

import java.util.Calendar

/*
 * Calendar-key helpers shared by missions, streaks and the reminder workers.
 *
 * A "day key" is yyyyMMdd as an Int (collation-friendly, easy to persist in prefs).
 * A "week key" is the day key of the Monday that starts the week — weeks are
 * Monday-anchored everywhere in the app (planning screen, weekly missions,
 * endurance totals) regardless of the device locale.
 */

fun dayKeyOf(c: Calendar): Int =
    c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH)

fun todayDayKey(): Int = dayKeyOf(Calendar.getInstance())

fun yesterdayDayKey(): Int =
    dayKeyOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) })

/** Calendar positioned at local midnight of the given day key. */
fun calendarOf(dayKey: Int): Calendar = Calendar.getInstance().apply {
    clear()
    set(dayKey / 10000, (dayKey / 100) % 100 - 1, dayKey % 100)
}

/** Moves [c] back to the Monday of its week (time-of-day untouched). */
fun Calendar.toMonday(): Calendar = apply {
    val back = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    add(Calendar.DAY_OF_MONTH, -back)
}

/** Week key = day key of this week's Monday. */
fun thisWeekKey(): Int = dayKeyOf(Calendar.getInstance().toMonday())

fun weekKeyOf(dayKey: Int): Int = if (dayKey <= 0) 0 else dayKeyOf(calendarOf(dayKey).toMonday())

/** Monday 00:00:00.000 → Sunday 23:59:59.999 of the current week, as epoch millis. */
fun thisWeekBounds(): Pair<Long, Long> {
    val c = Calendar.getInstance().toMonday()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    val start = c.timeInMillis
    c.add(Calendar.DAY_OF_MONTH, 7)
    c.add(Calendar.MILLISECOND, -1)
    return start to c.timeInMillis
}

/** 00:00:00.000 → 23:59:59.999 today, as epoch millis. */
fun todayBounds(): Pair<Long, Long> {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    val start = c.timeInMillis
    c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
    return start to c.timeInMillis
}

/**
 * Calendar days between two day keys (0 = same day, 1 = consecutive days).
 * Rounded rather than floored so a DST shift (23h / 25h day) can't skew the count.
 * Returns Int.MAX_VALUE when either key is unset so callers treat it as "no history".
 */
fun daysBetweenDayKeys(from: Int, to: Int): Int {
    if (from <= 0 || to <= 0) return Int.MAX_VALUE
    val a = calendarOf(from).timeInMillis
    val b = calendarOf(to).timeInMillis
    return Math.round((b - a) / 86_400_000.0).toInt()
}

/** Whole weeks between the weeks containing the two day keys (0 = same week). */
fun weeksBetweenDayKeys(from: Int, to: Int): Int {
    if (from <= 0 || to <= 0) return Int.MAX_VALUE
    return daysBetweenDayKeys(weekKeyOf(from), weekKeyOf(to)) / 7
}
