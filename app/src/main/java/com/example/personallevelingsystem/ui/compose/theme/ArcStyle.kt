package com.example.personallevelingsystem.ui.compose.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.personallevelingsystem.R

enum class ButtonStyle(@StringRes val labelRes: Int, @StringRes val blurbRes: Int) {
    GradientGlow(R.string.style_gradient, R.string.style_gradient_blurb),
    FlatShadow(R.string.style_flat, R.string.style_flat_blurb),
    Glass(R.string.style_glass, R.string.style_glass_blurb)
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
