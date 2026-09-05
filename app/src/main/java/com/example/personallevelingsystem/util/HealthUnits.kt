package com.example.personallevelingsystem.util

import java.util.Locale

/*
 * Single place that knows how sleep durations are stored.
 *
 * The sleep screen writes "HH:MM" (time picker), the notification quick-actions
 * historically wrote "7.0h", and older rows may hold a bare number. All of them
 * must read back as hours or the Recovery mission / bio-metrics silently show 0.
 */

fun parseSleepHours(raw: String?): Float {
    val s = raw?.trim()?.lowercase(Locale.ROOT) ?: return 0f
    if (s.isEmpty()) return 0f
    if (':' in s) {
        val parts = s.split(':')
        val h = parts[0].trim().toFloatOrNull() ?: return 0f
        val m = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0f
        return h + m / 60f
    }
    return s.removeSuffix("h").replace(',', '.').trim().toFloatOrNull() ?: 0f
}

/** Canonical "HH:MM" representation used when the app writes a sleep row itself. */
fun formatSleepDuration(hours: Float): String {
    val totalMinutes = Math.round(hours * 60f)
    return String.format(Locale.US, "%02d:%02d", totalMinutes / 60, totalMinutes % 60)
}
