package com.moncoran.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun AccueilSection(title: String, items: List<Triple<String, String, () -> Unit>>) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Green,
        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        items.forEach { (emoji, label, action) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .clickable { action() }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 32.sp)
                Spacer(Modifier.height(6.dp))
                Text(label, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

@Composable
fun AccueilScreen(
    onQuran: () -> Unit,
    onAudio: () -> Unit,
    onPrayer: () -> Unit,
    onQibla: () -> Unit,
    onTasbih: () -> Unit,
    onDua: () -> Unit,
    onProphets: () -> Unit,
    onCalendar: () -> Unit,
    onMore: () -> Unit
) {
    val context = LocalContext.current
    var showLang by remember { mutableStateOf(false) }
    if (showLang) {
        LanguageDialog(onDismiss = { showLang = false })
    }
    val shareMessage = tr(
        "Je te recommande cette application du Saint Coran : Mon Coran 📖 (Coran, horaires de prière, Qibla, invocations).",
        "I recommend this Quran app: Mon Coran 📖 (Quran, prayer times, Qibla, supplications).",
        "推荐这款古兰经应用：Mon Coran 📖（古兰经、礼拜时间、朝向、祈祷词）。",
        "أنصحك بهذا التطبيق للقرآن الكريم: Mon Coran 📖 (القرآن، مواقيت الصلاة، القبلة، الأذكار)."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.coran_accueil),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
                Spacer(Modifier.height(10.dp))
                Text("Coran", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    tr("Votre compagnon spirituel", "Your spiritual companion", "您的精神伴侣", "رفيقك الروحي"),
                    color = Gold
                )
            }
        }

        AccueilSection(
            tr("Coran et prière", "Quran & prayer", "古兰经与礼拜", "القرآن والصلاة"),
            listOf(
                Triple("📖", tr("Lire le Coran", "Read Quran", "阅读古兰经", "اقرأ القرآن"), onQuran),
                Triple("🎧", "Audio", onAudio),
                Triple("🕌", tr("Heures de prière", "Prayer times", "礼拜时间", "مواقيت الصلاة"), onPrayer),
                Triple("🧭", "Qibla", onQibla)
            )
        )

        AccueilSection(
            tr("Spiritualité", "Spirituality", "灵修", "الروحانيات"),
            listOf(
                Triple("📿", "Tasbih", onTasbih),
                Triple("🤲", tr("Invocations", "Supplications", "祈祷词", "الأذكار"), onDua),
                Triple("📜", tr("Prophètes", "Prophets", "先知", "الأنبياء"), onProphets),
                Triple("📅", tr("Calendrier", "Calendar", "日历", "التقويم"), onCalendar)
            )
        )

        AccueilSection(
            tr("Application", "App", "应用", "التطبيق"),
            listOf(
                Triple("📤", tr("Partager", "Share", "分享", "مشاركة"), { shareText(context, shareMessage) }),
                Triple(
                    if (isDarkMode) "☀️" else "🌙",
                    if (isDarkMode) tr("Mode clair", "Light mode", "浅色模式", "الوضع الفاتح")
                    else tr("Mode sombre", "Dark mode", "深色模式", "الوضع الداكن"),
                    {
                        isDarkMode = !isDarkMode
                        saveDarkMode(context, isDarkMode)
                    }
                ),
                Triple("🌍", tr("Langue", "Language", "语言", "اللغة"), { showLang = true }),
                Triple("☪", tr("Plus", "More", "更多", "المزيد"), onMore)
            )
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun QuranApp() {
    val context = LocalContext.current
    val quranResult = remember { runCatching { loadQuran(context) } }
    val surahs = quranResult.getOrDefault(emptyList())
    val error = quranResult.exceptionOrNull()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSurah by remember { mutableStateOf<SurahData?>(null) }
    var showTasbih by remember { mutableStateOf(false) }
    var showInvocations by remember { mutableStateOf(false) }
    var showProphets by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var fromHome by remember { mutableStateOf(false) }
var autoPlayNext by remember { mutableStateOf(false) }
    val tabs = listOf(
        tr("Accueil", "Home", "首页", "الرئيسية"),
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
                            showProphets = false
                            showCalendar = false
                            fromHome = false
                        },
                        icon = { Text(listOf("🏠", "📖", "🎧", "🕌", "🧭", "☪")[index]) },
                        label = { Text(title, maxLines = 1, fontSize = 11.sp) }
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
            when (selectedTab) {
                0 -> when {
                    showProphets -> ProphetsScreen(onBack = { showProphets = false })
                    showCalendar -> HijriCalendarScreen(onBack = { showCalendar = false })
                    else -> AccueilScreen(
                        onQuran = { selectedTab = 1 },
                        onAudio = { selectedTab = 2 },
                        onPrayer = { selectedTab = 3 },
                        onQibla = { selectedTab = 4 },
                        onTasbih = { fromHome = true; showTasbih = true; selectedTab = 5 },
                        onDua = { fromHome = true; showInvocations = true; selectedTab = 5 },
                        onProphets = { showProphets = true },
                        onCalendar = { showCalendar = true },
                        onMore = { selectedTab = 5 }
                    )
                }
            1 -> {
    if (selectedSurah == null) {
        QuranHome(
            surahs = surahs,
            error = error?.message,
            onSelect = { autoPlayNext = false; selectedSurah = it }
        )
    } else {
        key(selectedSurah!!.number) {
            SurahReader(
                surah = selectedSurah!!,
                onBack = { autoPlayNext = false; selectedSurah = null },
                autoPlay = autoPlayNext,
                onNext = {
                    val next = surahs.getOrNull(selectedSurah!!.number)
                    if (next != null) {
                        autoPlayNext = true
                        selectedSurah = next
                    }
                }
            )
        }
    }
            }
                2 -> AudioHome()
                3 -> PrayerHome()
                4 -> QiblaScreen()
                5 -> if (showTasbih) {
                    TasbihScreen(onBack = {
                        showTasbih = false
                        if (fromHome) { fromHome = false; selectedTab = 0 }
                    })
                } else if (showInvocations) {
                    InvocationsScreen(onBack = {
                        showInvocations = false
                        if (fromHome) { fromHome = false; selectedTab = 0 }
                    })
                } else {
                    MoreHome(onTasbih = { showTasbih = true }, onInvocations = { showInvocations = true })
                }
            }
        }
    }
}
