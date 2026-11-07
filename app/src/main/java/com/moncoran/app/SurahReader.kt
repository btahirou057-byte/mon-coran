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
