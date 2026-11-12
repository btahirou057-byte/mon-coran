package com.moncoran.app

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Dhikr(
    val id: String,
    val arabic: String,
    @StringRes val translation: Int,
    val count: Int
)

val sayyidIstighfar = Dhikr(
    "m2",
    "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
    R.string.dhikr_sayyid,
    1
)

val agrement = Dhikr(
    "m3",
    "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
    R.string.dhikr_agrement,
    3
)

val auNomDAllah = Dhikr(
    "m4",
    "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
    R.string.dhikr_bismillah,
    3
)

// À vérifier avec une source fiable (ex. Hisn al-Muslim) avant publication.
val adhkarMatin = listOf(
    Dhikr(
        "m1",
        "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        R.string.dhikr_morning_1,
        1
    ),
    sayyidIstighfar,
    agrement,
    auNomDAllah,
    Dhikr("m5", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", R.string.dhikr_tasbih, 100)
)

val adhkarSoir = listOf(
    Dhikr(
        "s1",
        "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        R.string.dhikr_evening_1,
        1
    ),
    sayyidIstighfar.copy(id = "s2"),
    Dhikr(
        "s3",
        "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        R.string.dhikr_refuge,
        3
    ),
    agrement.copy(id = "s4"),
    auNomDAllah.copy(id = "s5"),
    Dhikr("s6", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", R.string.dhikr_tasbih, 100)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvocationsScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val list = if (tab == 0) adhkarMatin else adhkarSoir

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invocations_title), color = Color.White) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Green)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(stringResource(R.string.tab_morning)) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text(stringResource(R.string.tab_evening)) }
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list, key = { it.id }) { dhikr ->
                    DhikrCard(dhikr)
                }
            }
        }
    }
}

@Composable
private fun DhikrCard(dhikr: Dhikr) {
    var remaining by rememberSaveable(dhikr.id) { mutableIntStateOf(dhikr.count) }
    val translation = stringResource(dhikr.translation)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = dhikr.arabic,
                fontSize = 22.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            if (translation.isNotBlank()) {
                Text(
                    text = translation,
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.repetitions, dhikr.count), fontSize = 13.sp)
                Button(
                    onClick = { if (remaining > 0) remaining-- },
                    enabled = remaining > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text(
                        if (remaining > 0) stringResource(R.string.remaining, remaining)
                        else stringResource(R.string.done)
                    )
                }
            }
            if (remaining != dhikr.count) {
                TextButton(onClick = { remaining = dhikr.count }) {
                    Text(stringResource(R.string.restart))
                }
            }
        }
    }
}
