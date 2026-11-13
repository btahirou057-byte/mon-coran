package com.moncoran.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val Green: Color get() = if (isDarkMode) Color(0xFF2E8F70) else Color(0xFF075B46)
internal val Gold = Color(0xFFD6AE58)
internal val Background: Color get() = if (isDarkMode) Color(0xFF121A17) else Color(0xFFF5F7F2)

data class AyahData(
    val number: Int,
    val text: String,
    val globalNumber: Int
)

data class SurahData(
    val number: Int,
    val name: String,
    val ayahs: List<AyahData>
)

private val surahNames = listOf(
    "الْفَاتِحَةُ", "الْبَقَرَةُ", "آلُ عِمْرَانَ", "النِّسَاءُ",
    "الْمَائِدَةُ", "الْأَنْعَامُ", "الْأَعْرَافُ", "الْأَنْفَالُ",
    "التَّوْبَةُ", "يُونُسُ", "هُودٌ", "يُوسُفُ",
    "الرَّعْدُ", "إِبْرَاهِيمُ", "الْحِجْرُ", "النَّحْلُ",
    "الْإِسْرَاءُ", "الْكَهْفُ", "مَرْيَمُ", "طَهَ",
    "الْأَنْبِيَاءُ", "الْحَجُّ", "الْمُؤْمِنُونَ", "النُّورُ",
    "الْفُرْقَانُ", "الشُّعَرَاءُ", "النَّمْلُ", "الْقَصَصُ",
    "الْعَنْكَبُوتُ", "الرُّومُ", "لُقْمَانُ", "السَّجْدَةُ",
    "الْأَحْزَابُ", "سَبَأٌ", "فَاطِرٌ", "يس",
    "الصَّافَّاتُ", "ص", "الزُّمَرُ", "غَافِرٌ",
    "فُصِّلَتْ", "الشُّورَى", "الزُّخْرُفُ", "الدُّخَانُ",
    "الْجَاثِيَةُ", "الْأَحْقَافُ", "مُحَمَّدٌ", "الْفَتْحُ",
    "الْحُجُرَاتُ", "ق", "الذَّارِيَاتُ", "الطُّورُ",
    "النَّجْمُ", "الْقَمَرُ", "الرَّحْمَٰنُ", "الْوَاقِعَةُ",
    "الْحَدِيدُ", "الْمُجَادَلَةُ", "الْحَشْرُ", "الْمُمْتَحَنَةُ",
    "الصَّفُّ", "الْجُمُعَةُ", "الْمُنَافِقُونَ", "التَّغَابُنُ",
    "الطَّلَاقُ", "التَّحْرِيمُ", "الْمُلْكُ", "الْقَلَمُ",
    "الْحَاقَّةُ", "الْمَعَارِجُ", "نُوحٌ", "الْجِنُّ",
    "الْمُزَّمِّلُ", "الْمُدَّثِّرُ", "الْقِيَامَةُ", "الْإِنْسَانُ",
    "الْمُرْسَلَاتُ", "النَّبَأُ", "النَّازِعَاتُ", "عَبَسَ",
    "التَّكْوِيرُ", "الْإِنْفِطَارُ", "الْمُطَفِّفِينَ", "الْإِنْشِقَاقُ",
    "الْبُرُوجُ", "الطَّارِقُ", "الْأَعْلَى", "الْغَاشِيَةُ",
    "الْفَجْرُ", "الْبَلَدُ", "الشَّمْسُ", "اللَّيْلُ",
    "الضُّحَى", "الشَّرْحُ", "التِّينُ", "الْعَلَقُ",
    "الْقَدْرُ", "الْبَيِّنَةُ", "الزَّلْزَلَةُ", "الْعَادِيَاتُ",
    "الْقَارِعَةُ", "التَّكَاثُرُ", "الْعَصْرُ", "الْهُمَزَةُ",
    "الْفِيلُ", "قُرَيْشٌ", "الْمَاعُونُ", "الْكَوْثَرُ",
    "الْكَافِرُونَ", "النَّصْرُ", "الْمَسَدُ", "الْإِخْلَاصُ",
    "الْفَلَقُ", "النَّاسُ"
)

fun loadQuran(context: android.content.Context): List<SurahData> {
    val grouped = linkedMapOf<Int, MutableList<AyahData>>()
    var global = 0

    context.assets.open("quran-uthmani.txt")
        .bufferedReader(Charsets.UTF_8).use { reader ->
            reader.forEachLine { line ->
                val parts = line.trim().split('|', limit = 3)
                if (parts.size == 3) {
                    val surahNumber = parts[0].trim()
                        .removePrefix("\uFEFF").toIntOrNull()
                    val ayahNumber = parts[1].trim().toIntOrNull()
                    val arabicText = parts[2].trim()

                    if (surahNumber != null &&
                        ayahNumber != null &&
                        arabicText.isNotEmpty()
                    ) {
                        global++
                        grouped.getOrPut(surahNumber) {
                            mutableListOf()
                        }.add(AyahData(ayahNumber, arabicText, global))
                    }
                }
            }
        }

    return grouped.map { (number, ayahs) ->
        SurahData(
            number = number,
            name = surahNames.getOrElse(number - 1) { "سورة $number" },
            ayahs = ayahs
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDarkMode = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark", false)
        appLang = loadLang(this)
        setContent {
            MaterialTheme(
                colorScheme = if (isDarkMode) darkColorScheme(primary = Green) else lightColorScheme(primary = Green)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = Background, contentColor = TextPrimary) {
                    QuranApp()
                }
            }
        }
    }
}

@Composable
fun QuranAppOld() {
    val context = LocalContext.current
    val quranResult = remember { runCatching { loadQuran(context) } }
    val surahs = quranResult.getOrDefault(emptyList())
    val error = quranResult.exceptionOrNull()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSurah by remember { mutableStateOf<SurahData?>(null) }
    var showTasbih by remember { mutableStateOf(false) }
    var showInvocations by remember { mutableStateOf(false) }
    val tabs = listOf(
        tr("Coran", "Quran", "古兰经", "القرآن"),
        tr("Audio", "Audio", "音频", "الصوتيات"),
        tr("Prières", "Prayers", "礼拜", "الصلاة"),
        tr("Qibla", "Qibla", "朝向", "القبلة"),
        tr("Plus", "More", "更多", "المزيد")
    )

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            showInvocations = false
                            selectedSurah = null
                            showTasbih = false
                        },
                        icon = { Text(listOf("📖", "🎧", "🕌", "🧭", "☪")[index]) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                0 -> {
                    if (selectedSurah == null) {
                        QuranHome(
                            surahs = surahs,
                            error = error?.message,
                            onSelect = { selectedSurah = it }
                        )
                    } else {
                        SurahReader(
                            surah = selectedSurah!!,
                            onBack = { selectedSurah = null }
                        )
                    }
                }
                1 -> AudioHome()
                2 -> PrayerHome()
                3 -> QiblaScreen()
                4 -> if (showTasbih) {
                    TasbihScreen(onBack = { showTasbih = false })
                } else if (showInvocations) {
                    InvocationsScreen(onBack = { showInvocations = false })
                } else {
                    MoreHome(onTasbih = { showTasbih = true }, onInvocations = { showInvocations = true })
                }
            }
        }
    }
}

@Composable
fun QuranHome(
    surahs: List<SurahData>,
    error: String?,
    onSelect: (SurahData) -> Unit
) {
    if (error != null) {
        Text(
            tr(
                "Erreur de lecture du Coran : $error",
                "Error reading the Quran: $error",
                "读取古兰经出错：$error",
                "خطأ في قراءة القرآن: $error"
            ),
            modifier = Modifier.padding(20.dp),
            color = Color.Red
        )
        return
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle(tr("Le Saint Coran", "The Holy Quran", "神圣的古兰经", "القرآن الكريم")) }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Green)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الْقُرْآنُ الْكَرِيمُ", color = Gold, fontSize = 30.sp)
                    Text(
                        tr(
                            "Lisez et méditez le Saint Coran",
                            "Read and reflect on the Holy Quran",
                            "阅读并思考古兰经",
                            "اقرأ القرآن الكريم وتدبّره"
                        ),
                        color = Color.White
                    )
                    Text(
                        tr(
                            "${surahs.size} sourates disponibles",
                            "${surahs.size} surahs available",
                            "共 ${surahs.size} 章可读",
                            "${surahs.size} سورة متاحة"
                        ),
                        color = Color.White
                    )
                }
            }
        }

        item { SectionTitle(tr("Les sourates", "Surahs", "章节", "السور")) }

        items(surahs) { surah ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(surah) },
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            tr(
                                "Sourate ${surah.number}",
                                "Surah ${surah.number}",
                                "第 ${surah.number} 章",
                                "سورة ${surah.number}"
                            ),
                            color = Green,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            tr(
                                "${surah.ayahs.size} versets",
                                "${surah.ayahs.size} verses",
                                "${surah.ayahs.size} 节",
                                "${surah.ayahs.size} آية"
                            ),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        surah.name,
                        color = Green,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
