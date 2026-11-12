package com.moncoran.app

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class Lang(val label: String) {
    FR("Français"), EN("English"), ZH("中文"), AR("العربية")
}

var appLang by mutableStateOf(Lang.FR)

fun tr(fr: String, en: String, zh: String, ar: String): String = when (appLang) {
    Lang.FR -> fr
    Lang.EN -> en
    Lang.ZH -> zh
    Lang.AR -> ar
}

fun loadLang(context: Context): Lang {
    val code = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getString("lang", "FR") ?: "FR"
    return runCatching { Lang.valueOf(code) }.getOrDefault(Lang.FR)
}

fun saveLang(context: Context, lang: Lang) {
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit().putString("lang", lang.name).apply()
}

@Composable
fun LanguageDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(tr("Langue", "Language", "语言", "اللغة")) },
        text = {
            Column {
                Lang.values().forEach { lang ->
                    TextButton(onClick = {
                        appLang = lang
                        saveLang(context, lang)
                        onDismiss()
                    }) {
                        Text((if (appLang == lang) "✓ " else "") + lang.label)
                    }
                }
            }
        }
    )
}
