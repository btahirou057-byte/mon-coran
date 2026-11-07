package com.moncoran.app

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Highlight = Color(0xFFDDEBD5)

private data class Reciter(val label: String, val id: String)

private val reciters = listOf(
    Reciter("Mishary Alafasy", "ar.alafasy"),
    Reciter("Abdul Rahman Al-Sudais", "ar.abdurrahmaansudais"),
    Reciter("Maher Al-Muaiqly", "ar.mahermuaiqly"),
    Reciter("Abdul Basit", "ar.abdulbasitmurattal")
)

private val speeds = listOf(1f, 1.25f, 1.5f, 0.75f)

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

        val url = "https://cdn.islamic.network/quran/audio/128/" +
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

    fun changeSpeed() {
speedIndex = if (speedIndex >= speeds.size - 1) 0 else speedIndex + 1
        try {
            val p = player
            if (p != null && isPlaying) {
                p.playbackParams = PlaybackParams().setSpeed(speeds[speedIndex])
            }
        } catch (_: Exception) {
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

        PlayerBar(
            reciterLabel = reciters[reciterIndex].label,
            reciterNames = reciters.map { it.label },
            statusText = if (currentIndex >= 0)
                "Verset ${surah.ayahs[currentIndex].number} / ${surah.ayahs.size}"
            else "Touche un verset ou appuie sur ▶",
            isPlaying = isPlaying,
            isPreparing = isPreparing,
            repeatOne = repeatOne,
            speedLabel = "${speeds[speedIndex]}x",
            menuOpen = menuOpen,
            onMenuOpen = { menuOpen = it },
            onPickReciter = { i ->
                menuOpen = false
                reciterIndex = i
                if (currentIndex >= 0 && (isPlaying || isPreparing)) {
                    playAyah(currentIndex)
                }
            },
            onRepeat = { repeatOne = !repeatOne },
            onPrev = { if (currentIndex > 0) playAyah(currentIndex - 1) },
            onToggle = { togglePlayPause() },
            onNext = { if (currentIndex >= 0) playAyah(currentIndex + 1) },
            onSpeed = { changeSpeed() },
            onStop = {
                stopAudio()
                currentIndex = -1
                audioError = null
            }
        )
    }
}
