package com.moncoran.app

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



data class Dhikr(
    val id: String,
    val arabic: String,
    val french: String,
    val count: Int
)

val sayyidIstighfar = Dhikr(
    "m2",
    "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
    "Ô Allah, Tu es mon Seigneur, il n'y a de divinité digne d'adoration que Toi. Tu m'as créé et je suis Ton serviteur, fidèle à mon engagement envers Toi autant que je le peux. Je cherche refuge auprès de Toi contre le mal que j'ai commis. Je reconnais Tes bienfaits envers moi et je reconnais mon péché : pardonne-moi, car nul ne pardonne les péchés en dehors de Toi.",
    1
)

val agrement = Dhikr(
    "m3",
    "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
    "J'agrée Allah comme Seigneur, l'Islam comme religion et Muhammad ﷺ comme Prophète.",
    3
)

val auNomDAllah = Dhikr(
    "m4",
    "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
    "Au nom d'Allah, avec le nom duquel rien ne peut nuire, ni sur terre ni dans le ciel. Il est l'Audient, l'Omniscient.",
    3
)

// À vérifier avec une source fiable (ex. Hisn al-Muslim) avant publication.
val adhkarMatin = listOf(
    Dhikr(
        "m1",
        "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        "Nous voici au matin et la royauté appartient à Allah. Louange à Allah. Il n'y a de divinité digne d'adoration qu'Allah, Seul, sans associé.",
        1
    ),
    sayyidIstighfar,
    agrement,
    auNomDAllah,
    Dhikr("m5", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Gloire et louange à Allah.", 100)
)

val adhkarSoir = listOf(
    Dhikr(
        "s1",
        "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        "Nous voici au soir et la royauté appartient à Allah. Louange à Allah. Il n'y a de divinité digne d'adoration qu'Allah, Seul, sans associé.",
        1
    ),
    sayyidIstighfar.copy(id = "s2"),
    Dhikr(
        "s3",
        "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        "Je me réfugie auprès des paroles parfaites d'Allah contre le mal de ce qu'Il a créé.",
        3
    ),
    agrement.copy(id = "s4"),
    auNomDAllah.copy(id = "s5"),
    Dhikr("s6", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Gloire et louange à Allah.", 100)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvocationsScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val list = if (tab == 0) adhkarMatin else adhkarSoir

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invocations", color = Color.White) },
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
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Matin") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Soir") })
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

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
            Text(
                text = dhikr.french,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Répétitions : ${dhikr.count}", fontSize = 13.sp)
                Button(
                    onClick = { if (remaining > 0) remaining-- },
                    enabled = remaining > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text(if (remaining > 0) "$remaining restant(s)" else "Terminé ✓")
                }
            }
            if (remaining != dhikr.count) {
                TextButton(onClick = { remaining = dhikr.count }) { Text("Recommencer") }
            }
        }
    }
}
