package com.moncoran.app

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
private val Highlight = Color(0xFFDDEBD5)

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

data class Reciter(val label: String, val id: String)

private val reciters = listOf(
    Reciter("Mishary Alafasy", "ar.alafasy"),
    Reciter("Abdul Rahman Al-Sudais", "ar.abdurrahmaansudais"),
    Reciter("Maher Al-Muaiqly", "ar.mahermuaiqly"),
    Reciter("Abdul Basit", "ar.abdulbasitmurattal")
)

private val speeds = listOf(1f, 1.25f, 1.5f, 0.75f)

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
    var audioError by remember { mutableStateOf<String?>(null) }

    var currentIndex by remember { mutableIntStateOf(-1) }
    var repeatOne by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(0) }
    var reciterIndex by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    fun stopAudio() {
        player?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: Exception) {
            }
            it.release()
        }
        player = null
        isPlaying = false
        isPreparing = false
    }

    fun playAyah(index: Int) {
        if (index !in surah.ayahs.indices) {
            stopAudio()
            currentIndex = -1
            return
        }

        player?.let {
            try {
                it.release()
            } catch (_: Exception) {
            }
        }
        player = null

        currentIndex = index
        isPreparing = true
        isPlaying = false
        audioError = null

        val url =
            "https://cdn.islamic.network/quran/audio/128/" +
                "${reciters[reciterIndex].id}/${surah.ayahs[index].globalNumber}.mp3"

        try {
            val mp = MediaPlayer()
            player = mp

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            mp.setOnPreparedListener { m ->
                isPreparing = false
                try {
                    m.playbackParams = PlaybackParams().setSpeed(speeds[speedIndex])
                } catch (_: Exception) {
                }
                m.start()
                isPlaying = true
            }

            mp.setOnCompletionListener {
                if (repeatOne) playAyah(index) else playAyah(index + 1)
            }

            mp.setOnErrorListener { _, what, extra ->
                audioError = "Erreur de lecture ($what, $extra). Vérifie Internet."
                stopAudio()
                true
            }

            mp.setDataSource(url)
            mp.prepareAsync()
        } catch (e: Exception) {
            stopAudio()
            audioError = "Erreur audio : ${e.message ?: "connexion impossible"}"
        }
    }

    fun togglePlayPause() {
        val p = player
        if (p != null && !isPreparing) {
            try {
                if (isPlaying) {
                    p.pause()
                    isPlaying = false
                } else {
                    p.start()
                    isPlaying = true
                }
            } catch (_: Exception) {
                playAyah(maxOf(currentIndex, 0))
            }
        } else if (p == null) {
            playAyah(maxOf(currentIndex, 0))
        }
    }

    DisposableEffect(surah.number) {
        onDispose {
            player?.release()
            player = null
        }
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "‹ Retour",
            modifier = Modifier
                .clickable {
                    stopAudio()
                    onBack()
                }
                .padding(16.dp),
            color = Green,
            fontWeight = FontWeight.Bold
        )

        Text(
            "سورة ${surah.name}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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

        if (audioError != null) {
            Text(
                audioError!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(surah.ayahs) { index, ayah ->
                Text(
                    "${ayah.text} ۝${ayah.number}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index == currentIndex) Highlight
                            else Color.Transparent
                        )
                        .clickable { playAyah(index) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    color = Color(0xFF202820),
                    fontSize = 25.sp,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Right
                )

                HorizontalDivider(color = Color(0xFFE5E8E1))
            }
        }

        // Barre du lecteur
        Surface(
            color = Color(0xFF111111),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Text(
                        "🎙 ${reciters[reciterIndex].label}  ▾",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { menuOpen = true }
                            .padding(6.dp)
                    )
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        reciters.forEachIndexed { i, r ->
                            DropdownMenuItem(
                                text = { Text(r.label) },
                                onClick = {
                                    menuOpen = false
                                    reciterIndex = i
                                    if (currentIndex >= 0 && (isPlaying || isPreparing)) {
                                        playAyah(currentIndex)
                                    }
                                }
                            )
                        }
                    }
                }

                Text(
                    if (currentIndex >= 0)
                        "Verset ${surah.ayahs[currentIndex].number} / ${surah.ayahs.size}"
                    else "Touche un verset ou appuie sur ▶",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        repeatOne = !repeatOne
                    }) {
                        Text(
                            "🔁",
                            fontSize = 22.sp,
                            color = if (repeatOne) Gold else Color.Gray
                        )
                    }

                    TextButton(onClick = {
                        if (currentIndex > 0) playAyah(currentIndex - 1)
                    }) { Text("⏮", fontSize = 22.sp, color = Color.White) }

                    Button(
                        onClick = { togglePlayPause() },
                        enabled = !isPreparing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Green,
                            disabledContainerColor = Color.LightGray,
                            disabledContentColor = Green
                        )
                    ) {
                        Text(
                            when {
                                isPreparing -> "…"
                                isPlaying -> "⏸"
                                else -> "▶"
                            },
                            fontSize = 20.sp
                        )
                    }

                    TextButton(onClick = {
                        if (currentIndex >= 0) playAyah(currentIndex + 1)
                    }) { Text("⏭", fontSize = 22.sp, color = Color.White) }

                    TextButton(onClick = {
                        speedIndex = (speedIndex + 1) % speeds.size
                        try {
                            player?.let {
