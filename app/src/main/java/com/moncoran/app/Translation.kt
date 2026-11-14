package com.moncoran.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// "off" = arabe seul, sinon "fr", "en" ou "zh"
internal var translationLang by mutableStateOf("off")

private fun editionFor(lang: String): String = when (lang) {
    "fr" -> "fr.hamidullah"
    "en" -> "en.pickthall"
    "zh" -> "zh.jian"
    else -> ""
}

// Charge la traduction d'une sourate (une ligne par verset).
// Le résultat est gardé sur le téléphone : ensuite ça marche sans internet.
internal suspend fun loadTranslation(context: Context, surah: Int, lang: String): List<String>? =
    withContext(Dispatchers.IO) {
        val edition = editionFor(lang)
        if (edition.isEmpty()) return@withContext null
        val dir = File(context.filesDir, "translations").apply { mkdirs() }
        val file = File(dir, "${edition}_$surah.json")
        try {
            val json = if (file.exists() && file.length() > 0) {
                file.readText()
            } else {
                val conn = URL("https://api.alquran.cloud/v1/surah/$surah/$edition")
                    .openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 15000
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                file.writeText(body)
                body
            }
            val ayahs = JSONObject(json).getJSONObject("data").getJSONArray("ayahs")
            List(ayahs.length()) { i -> ayahs.getJSONObject(i).getString("text") }
        } catch (e: Exception) {
            file.delete()
            null
        }
    }

@Composable
fun TranslationPicker() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        translationLang = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("translation", "off") ?: "off"
    }

    val options = listOf(
        "off" to tr("Aucune", "None", "无", "بدون"),
        "fr" to "Français",
        "en" to "English",
        "zh" to "中文"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tr("Traduction :", "Translation:", "翻译：", "الترجمة:"),
            fontSize = 13.sp,
            color = Color.DarkGray
        )
        options.forEach { (code, label) ->
            val selected = translationLang == code
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Green,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) Green else Color(0xFFE5E8E1))
                    .clickable {
                        translationLang = code
                        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .edit().putString("translation", code).apply()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
