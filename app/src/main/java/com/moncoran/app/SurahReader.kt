package com.moncoran.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Highlight = Color(0xFFDDEBD5)

private const val BISMILLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"

internal data class Reciter(val label: String, val id: String)

internal val reciters = listOf(
    Reciter("Mishary Alafasy", "ar.alafasy"),
    Reciter("Abdul Rahman Al-Sudais", "ar.abdurrahmaansudais"),
    Reciter("Maher Al-Muaiqly", "ar.mahermuaiqly"),
    Reciter("Abdul Basit", "ar.abdulbasitmurattal")
)

// Récitateur partagé entre l'onglet Audio et le lecteur de sourate
internal var selectedReciterIndex by mutableIntStateOf(0)

private val speeds = listOf(1f, 1.25f, 1.5f, 0.75f)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurahReader(
    surah: SurahData,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPreparing by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableIntStateOf(-1) }
    var bismillahActive by remember { mutableStateOf(false) }
    var repeatOne by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }

    val hasBismillah = surah.number != 1 && surah.number != 9

    val prefs = remember { context.getSharedPreferences("favoris", Context.MODE_PRIVATE) }
    var favorites by remember {
        mutableStateOf<Set<String>>(prefs.getStringSet("verses", emptySet())?.toSet() ?: emptySet())
    }
    var actionIndex by remember { mutableIntStateOf(-1) }
    var selectIndex by remember { mutableIntStateOf(-1) }

    fun toggleFavorite(key: String) {
        val updated = favorites.toMutableSet()
        if (!updated.add(key)) updated.remove(key)
        favorites = updated
        prefs.edit().putStringSet("verses", updated).apply()
    }

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
        bismillahActive = false
    }

    fun startPlayer(url: String, onDone: () -> Unit) {
        player?.let {
            try {
                it.release()
            } catch (_: Exception) {
            }
        }
        player = null

        isPreparing = true
        isPlaying = false
        audioError = null

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

            mp.setOnCompletionListener { onDone() }

            mp.setOnErrorListener { _, what, extra ->
                audioError = context.getString(R.string.err_playback, what, extra)
                stopAudio()
                true
            }

            mp.setDataSource(url)
            mp.prepareAsync()
        } catch (e: Exception) {
            stopAudio()
            audioError = context.getString(
                R.string.err_audio,
                e.message ?: context.getString(R.string.err_no_connection)
            )
        }
    }

    fun playAyah(index: Int) {
        if (index !in surah.ayahs.indices) {
            stopAudio()
            currentIndex = -1
            return
        }

        bismillahActive = false
        currentIndex = index

        val url = "https://cdn.islamic.network/quran/audio/128/" +
            "${reciters[selectedReciterIndex].id}/${surah.ayahs[index].globalNumber}.mp3"

        startPlayer(url) {
            if (repeatOne) playAyah(index) else playAyah(index + 1)
        }
    }

    fun playBismillah() {
        bismillahActive = true
        currentIndex = -1

        // Le numéro global 1 correspond à la Bismillah d'Al-Fatiha
        val url = "https://cdn.islamic.network/quran/audio/128/" +
            "${reciters[selectedReciterIndex].id}/1.mp3"

        startPlayer(url) { playAyah(0) }
    }

    fun playFromStart() {
        if (hasBismillah) playBismillah() else playAyah(0)
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
                if (currentIndex >= 0) playAyah(currentIndex) else playFromStart()
            }
        } else if (p == null) {
            if (currentIndex >= 0) playAyah(currentIndex) else playFromStart()
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
            listState.animateScrollToItem(currentIndex + if (hasBismillah) 1 else 0)
        }
    }

    if (actionIndex in surah.ayahs.indices) {
        val ayah = surah.ayahs[actionIndex]
        val key = "${surah.number}:${ayah.number}"
        val verseText = "${ayah.text}\n(${surah.name} : ${ayah.number})"
        val isFavorite = key in favorites

        AlertDialog(
            onDismissRequest = { actionIndex = -1 },
            confirmButton = {},
            title = {
                Text(stringResource(R.string.verse_status, ayah.number, surah.ayahs.size))
            },
            text = {
                Column {
                    TextButton(onClick = {
                        selectIndex = actionIndex
                        actionIndex = -1
                    }) { Text(stringResource(R.string.copy_part), color = Green) }

                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("verset", verseText))
                        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                        actionIndex = -1
                    }) { Text(stringResource(R.string.copy), color = Green) }

                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, verseText)
                        }
                        val chooser = Intent.createChooser(send, null)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                        actionIndex = -1
                    }) { Text(stringResource(R.string.share), color = Green) }

                    TextButton(onClick = {
                        toggleFavorite(key)
                        actionIndex = -1
                    }) {
                        Text(
                            stringResource(
                                if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                            ),
                            color = Green
                        )
                    }
                }
            }
        )
    }

    if (selectIndex in surah.ayahs.indices) {
        AlertDialog(
            onDismissRequest = { selectIndex = -1 },
            confirmButton = {
                TextButton(onClick = { selectIndex = -1 }) {
                    Text(stringResource(R.string.close), color = Green)
                }
            },
            title = { Text(stringResource(R.string.select_hint), fontSize = 14.sp) },
            text = {
                SelectionContainer {
                    Text(
                        surah.ayahs[selectIndex].text,
                        fontSize = 25.sp,
                        lineHeight = 44.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.back),
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
            stringResource(R.string.verses_count, surah.ayahs.size),
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
            if (hasBismillah) {
                item {
                    Text(
                        BISMILLAH,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (bismillahActive) Highlight
                                else Color.Transparent
                            )
                            .clickable { playBismillah() }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        color = Green,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(color = Color(0xFFE5E8E1))
                }
            }

            itemsIndexed(surah.ayahs) { index, ayah ->
                val star = if ("${surah.number}:${ayah.number}" in favorites) " ★" else ""
                Text(
                    "${ayah.text} ۝${ayah.number}$star",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index == currentIndex) Highlight
                            else Color.Transparent
                        )
                        .combinedClickable(
                            onClick = { playAyah(index) },
                            onLongClick = { actionIndex = index }
                        )
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
            reciterLabel = reciters[selectedReciterIndex].label,
            reciterNames = reciters.map { it.label },
            statusText = if (bismillahActive)
                BISMILLAH
            else if (currentIndex >= 0)
                stringResource(
                    R.string.verse_status,
                    surah.ayahs[currentIndex].number,
                    surah.ayahs.size
                )
            else stringResource(R.string.touch_hint),
            isPlaying = isPlaying,
            isPreparing = isPreparing,
            repeatOne = repeatOne,
            speedLabel = "${speeds[speedIndex]}x",
            menuOpen = menuOpen,
            onMenuOpen = { menuOpen = it },
            onPickReciter = { i ->
                menuOpen = false
                selectedReciterIndex = i
                if (bismillahActive) {
                    playBismillah()
                } else if (currentIndex >= 0 && (isPlaying || isPreparing)) {
                    playAyah(currentIndex)
                }
            },
            onRepeat = { repeatOne = !repeatOne },
            onPrev = {
                if (currentIndex > 0) playAyah(currentIndex - 1)
                else if (currentIndex == 0 && hasBismillah) playBismillah()
            },
            onToggle = { togglePlayPause() },
            onNext = {
                if (bismillahActive) playAyah(0)
                else if (currentIndex >= 0) playAyah(currentIndex + 1)
            },
            onSpeed = { changeSpeed() },
            onStop = {
                stopAudio()
                currentIndex = -1
                audioError = null
            }
        )
    }
}
