package com.example.personallevelingsystem.ui.compose.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ButtonStyle(val label: String, val blurb: String) {
    GradientGlow("Gradient glow", "Red to violet pill with a soft glow. The signature."),
    FlatShadow("Flat", "Solid red pill with a quiet drop shadow."),
    Glass("Glass", "Translucent pill, hairline edge, accent icon.")
}

/** App-wide visual choices the user can flip from the Style Lab; persisted in SharedPreferences. */
object ArcStyle {
    private const val PREFS = "arc_style"
    private const val KEY_BUTTON = "button_style"

    var buttonStyle: ButtonStyle by mutableStateOf(ButtonStyle.GradientGlow)
        private set

    fun load(context: Context) {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_BUTTON, null)
        buttonStyle = ButtonStyle.entries.firstOrNull { it.name == saved } ?: ButtonStyle.GradientGlow
    }

    fun setButtonStyle(context: Context, style: ButtonStyle) {
        buttonStyle = style
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_BUTTON, style.name).apply()
    }
}
