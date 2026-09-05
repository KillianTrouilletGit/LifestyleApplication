package com.example.personallevelingsystem.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

fun View.hapticTap() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}

fun View.hapticConfirm() {
    val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        HapticFeedbackConstants.CONFIRM
    } else {
        HapticFeedbackConstants.CONTEXT_CLICK
    }
    performHapticFeedback(constant)
}

fun View.hapticReward() {
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}
