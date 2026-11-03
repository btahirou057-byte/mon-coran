package com.moncoran.app
import android.os.Bundle
import android.media.MediaPlayer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF075B46)
private val Gold = Color(0xFFD6AE58)
private val Background = Color(0xFFF5F7F2)

data class AyahData(
    val number: Int,
    val text: String
)

data class SurahData(
    val number: Int,
    val name: String,
    val ayahs: List<AyahData>
)

private val surahNames = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء",
    "المائدة", "الأنعام", "الأعراف", "الأنفال",
    "التوبة", "يونس", "هود", "يوسف",
    "الرعد", "إبراهيم", "الحجر", "النحل",
    "الإسراء", "الكهف", "مريم", "طه",
    "الأنبياء", "الحج", "المؤمنون", "النور",
    "الفرقان", "الشعراء", "النمل", "القصص",
    "العنكبوت", "الروم", "لقمان", "السجدة",
    "الأحزاب", "سبأ", "فاطر", "يس",
    "الصافات", "ص", "الزمر", "غافر",
    "فصلت", "الشورى", "الزخرف", "الدخان",
    "الجاثية", "الأحقاف", "محمد", "الفتح",
    "الحجرات", "ق", "الذاريات", "الطور",
    "النجم", "القمر", "الرحمن", "الواقعة",
    "الحديد", "المجادلة", "الحشر", "الممتحنة",
    "الصف", "الجمعة", "المنافقون", "التغابن",
    "الطلاق", "التحريم", "الملك", "القلم",
    "الحاقة", "المعارج", "نوح", "الجن",
    "المزمل", "المدثر", "القيامة", "الإنسان",
    "المرسلات", "النبأ", "النازعات", "عبس",
    "التكوير", "الانفطار", "المطففين", "الانشقاق",
    "البروج", "الطارق", "الأعلى", "الغاشية",
    "الفجر", "البلد", "الشمس", "الليل",
    "الضحى", "الشرح", "التين", "العلق",
    "القدر", "البينة", "الزلزلة", "العاديات",
    "القارعة", "التكاثر", "العصر", "الهمزة",
    "الفيل", "قريش", "الماعون", "الكوثر",
    "الكافرون", "النصر", "المسد", "الإخلاص",
    "الفلق", "الناس"
)

fun loadQuran(context: android.content.Context): List<SurahData> {
    val grouped = linkedMapOf<Int, MutableList<AyahData>>()

    context.assets.open("quran-uthmani.txt").bufferedReader(Charsets.UTF_8).use { reader ->
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
                    grouped.getOrPut(surahNumber) {
                        mutableListOf()
                    }.add(AyahData(ayahNumber, arabicText))
                }
            }
        }
    }

    return grouped.map { (number, ayahs) ->
        SurahData(
            number = number,
            name = surahNames.getOrElse(number - 1) {
                "سورة $number"
            },
            ayahs = ayahs
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = Green)
            ) {
                QuranApp()
            }
        }
    }
}

@Composable
fun QuranApp() {
    val context = LocalContext.current

    val quranResult = remember {
        runCatching { loadQuran(context) }
    }

    val surahs = quranResult.getOrDefault(emptyList())
    val error = quranResult.exceptionOrNull()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSurah by remember {
        mutableStateOf<SurahData?>(null)
    }

    val tabs = listOf("Coran", "Audio", "Prières", "Plus")

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            selectedSurah = null
                        },
                        icon = {
                            Text(
                                listOf("📖", "🎧", "🕌", "☪")[index]
                            )
                        },
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
        ) {
            Header()

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
                3 -> MoreHome()
            }
        }
    }
}

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Green)
            .padding(22.dp)
    ) {
        Text(
            "السَّلَامُ عَلَيْكُمْ",
            color = Gold,
            fontSize = 24.sp
        )

        Text(
            "القرآن الكريم",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Votre compagnon spirituel",
            color = Color.White
        )
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
            "Erreur de lecture du Coran : $error",
            modifier = Modifier.padding(20.dp),
            color = Color.Red
        )
        return
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTitle("Le Saint Coran")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Green
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "الْقُرْآنُ الْكَرِيمُ",
                        color = Gold,
                        fontSize = 30.sp
                    )
                    Text(
                        "Lisez et méditez le Saint Coran",
                        color = Color.White
                    )
                    Text(
                        "${surahs.size} sourates disponibles",
                        color = Color.White
                    )
                }
            }
        }

        item {
            SectionTitle("Les sourates")
        }

        items(surahs) { surah ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(surah) },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
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
                            "Sourate ${surah.number}",
                            color = Green,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${surah.ayahs.size} versets",
                            color = Color.DarkGray,
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

@Composable
fun SurahReader(
    surah: SurahData,
    onBack: () -> Unit
) {
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPreparing by remember { mutableStateOf(false) }

    DisposableEffect(surah.number) {
        onDispose {
            player?.release()
            player = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "‹ Retour",
            modifier = Modifier
                .clickable { onBack() }
                .padding(16.dp),
            color = Green,
            fontWeight = FontWeight.Bold
        )

        Text(
            "سورة ${surah.name}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            color = Green,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            "${surah.ayahs.size} versets",
            modifier = Modifier.fillMaxWidth(),
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (isPlaying) {
                        player?.pause()
                        isPlaying = false
                    } else if (player != null) {
                        player?.start()
                        isPlaying = true
                    } else {
                        try {
                            isPreparing = true

                            val audio = MediaPlayer()
                            player = audio

                            val url =
                                "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/${surah.number}.mp3"

                            audio.setDataSource(url)

                            audio.setOnPreparedListener {
                                isPreparing = false
                                it.start()
                                isPlaying = true
                            }

                            audio.setOnCompletionListener {
                                isPlaying = false
                                it.release()
                                player = null
                            }

                            audio.setOnErrorListener { mp, _, _ ->
                                isPlaying = false
                                isPreparing = false
                                mp.release()
                                player = null
                                true
                            }

                            audio.prepareAsync()
                        } catch (e: Exception) {
                            isPreparing = false
                            player?.release()
                            player = null
                        }
                    }
                },
                enabled = !isPreparing
            ) {
                Text(
                    when {
                        isPreparing -> "Chargement..."
                        isPlaying -> "⏸ Pause"
                        else -> "▶ Jouer"
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    player?.release()
                    player = null
                    isPlaying = false
                    isPreparing = false
                }
            ) {
                Text("⏹ Arrêter")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(surah.ayahs) { ayah ->
                Text(
                    "${ayah.text} ۝${ayah.number}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = Color(0xFF202820),
                    fontSize = 25.sp,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Right
                )

                HorizontalDivider(
                    color = Color(0xFFE5E8E1)
                )
            }
        }
    }
}

@Composable
fun AudioHome() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle("Récitateurs du Coran") }
        item { FeatureCard("🎧 Mishary Alafasy", "Récitations du Coran") }
        item { FeatureCard("🎧 Abdul Rahman Al-Sudais", "Récitations du Coran") }
        item { FeatureCard("🎧 Maher Al-Muaiqly", "Récitations du Coran") }
        item { FeatureCard("🎧 Abdul Basit", "Récitations du Coran") }
        item {
            Text("Les lecteurs audio seront ajoutés dans une prochaine étape.")
        }
    }
}

@Composable
fun PrayerHome() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle("Heures de prière") }
        item { FeatureCard("Fajr", "Prière de l'aube") }
        item { FeatureCard("Dhuhr", "Prière de midi") }
        item { FeatureCard("Asr", "Prière de l'après-midi") }
        item { FeatureCard("Maghrib", "Prière du coucher du soleil") }
        item { FeatureCard("Isha", "Prière de la nuit") }
        item { FeatureCard("🧭 Qibla", "Direction de La Mecque") }
    }
}

@Composable
fun MoreHome() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle("Autres fonctionnalités") }
        item { FeatureCard("📿 Tasbih", "Compteur de glorifications") }
        item { FeatureCard("🤲 Invocations", "Adhkar du matin et du soir") }
        item { FeatureCard("📜 Histoires des prophètes", "Découvrir les prophètes") }
        item { FeatureCard("🌙 Mode sombre", "Personnaliser l'affichage") }
        item { FeatureCard("🌍 Langue", "Arabe et français") }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Green,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun FeatureCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = Color.DarkGray
            )
        }
    }
}
