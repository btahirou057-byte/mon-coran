package com.moncoran.app
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
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
    var repeatOne by remember { mutableStateOf(false) }
    var speedIndex by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }

    // La Bismillah est dans le texte du verset 1 (sauf sourates 1 et 9)
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
val scope = rememberCoroutineScope()
val token = remember { intArrayOf(0) }
    fun stopAudio() {
        token[0]++
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
        val my = ++token[0]

        scope.launch {
            val file = fetchAudio(context, url)
            if (my != token[0]) return@launch
            if (file == null) {
                isPreparing = false
                audioError = context.getString(R.string.err_no_connection)
                return@launch
            }
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
                    file.delete()
                    audioError = context.getString(R.string.err_playback, what, extra)
                    stopAudio()
                    true
                }
                mp.setDataSource(file.absolutePath)
                mp.prepareAsync()
            } catch (e: Exception) {
                stopAudio()
                audioError = context.getString(
                    R.string.err_audio,
                    e.message ?: context.getString(R.string.err_no_connection)
                )
            }
        }
    }
    fun playAyah(index: Int, withBismillah: Boolean = true) {
        if (index !in surah.ayahs.indices) {
            stopAudio()
            currentIndex = -1
            return
        }

        currentIndex = index

        val base = "https://cdn.islamic.network/quran/audio/128/" +
            "${reciters[selectedReciterIndex].id}/"
        val verseUrl = base + "${surah.ayahs[index].globalNumber}.mp3"
if (index + 1 in surah.ayahs.indices) {
    scope.launch {
        fetchAudio(context, base + "${surah.ayahs[index + 1].globalNumber}.mp3")
    }
}
        val afterVerse = {
            if (repeatOne) playAyah(index, false) else playAyah(index + 1)
        }

        if (withBismillah && index == 0 && hasBismillah) {
            // Le fichier 1.mp3 est la Bismillah récitée
            startPlayer(base + "1.mp3") {
                startPlayer(verseUrl) { afterVerse() }
            }
        } else {
            startPlayer(verseUrl) { afterVerse() }
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

            token[0]++
            player?.release()
            player = null
        }
    }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
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
            itemsIndexed(surah.ayahs) { index, ayah ->
                val star = if ("${surah.number}:${ayah.number}" in favorites) " ★" else ""
                Text(
                    text = buildAnnotatedString {
    append(ayah.text)
    append(" ")
    appendInlineContent("badge", ayah.number.toString())
    append(star)
},
inlineContent = ayahInline,
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
            statusText = if (currentIndex >= 0)
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
@Composable
private fun AyahBadge(number: String) {
    val color = Color(0xFF202820)
    Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color, style = Stroke(width = 1.5.dp.toPx()))
        }
        Text(
            text = number.map { '٠' + (it - '0') }.joinToString(""),
            fontSize = 13.sp,
            color = color
        )
    }
}

private val ayahInline = mapOf(
    "badge" to InlineTextContent(
        Placeholder(36.sp, 36.sp, PlaceholderVerticalAlign.Center)
    ) { num -> AyahBadge(num) }
)
private suspend fun fetchAudio(context: Context, url: String): File? =
    withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "audio").apply { mkdirs() }
        val file = File(dir, url.substringAfter("/audio/").replace("/", "_"))
        if (file.exists() && file.length() > 0) return@withContext file
        repeat(4) { attempt ->
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val tmp = File(dir, file.name + "." + System.nanoTime() + ".tmp")
                conn.inputStream.use { input ->
                    tmp.outputStream().use { input.copyTo(it) }
                }
                if (tmp.length() > 0 && tmp.renameTo(file)) return@withContext file
                tmp.delete()
            } catch (_: Exception) {
            }
            delay(1000L * (attempt + 1))
        }
        null
    }
