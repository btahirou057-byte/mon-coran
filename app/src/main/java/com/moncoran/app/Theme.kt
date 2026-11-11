package com.moncoran.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

var isDarkMode by mutableStateOf(false)

val CardBg: Color get() = if (isDarkMode) Color(0xFF1F2B27) else Color.White
val TextPrimary: Color get() = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF1C1B1F)
val TextSecondary: Color get() = if (isDarkMode) Color(0xFFB8C4BF) else Color.DarkGray

fun saveDarkMode(context: Context, value: Boolean) {
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit().putBoolean("dark", value).apply()
}
