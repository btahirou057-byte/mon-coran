package com.moncoran.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green = Color(0xFF075B46)
private val Gold = Color(0xFFD6AE58)
private val Background = Color(0xFFF5F7F2)

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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Coran", "Audio", "Prières", "Plus")

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
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
                0 -> QuranHome()
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
            "MON CORAN",
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
fun QuranHome() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        "114 sourates",
                        color = Color.White
                    )
                }
            }
        }
        item {
            FeatureCard(
                "📖 Lecture du Coran",
                "Retrouvez votre dernière lecture"
            )
        }
        item {
            FeatureCard(
                "📚 Les 114 sourates",
                "Accéder aux sourates"
            )
        }
        item {
            FeatureCard(
                "⭐ Mes favoris",
                "Versets enregistrés"
            )
        }
        item {
            FeatureCard(
                "🔎 Recherche",
                "Rechercher une sourate"
            )
        }
    }
}

@Composable
fun AudioHome() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Récitateurs du Coran")
        }
        item {
            FeatureCard(
                "🎧 Mishary Alafasy",
                "Récitations du Coran"
            )
        }
        item {
            FeatureCard(
                "🎧 Abdul Rahman Al-Sudais",
                "Récitations du Coran"
            )
        }
        item {
            FeatureCard(
                "🎧 Maher Al-Muaiqly",
                "Récitations du Coran"
            )
        }
        item {
            FeatureCard(
                "🎧 Abdul Basit",
                "Récitations du Coran"
            )
        }
        item {
            Text(
                "Les lecteurs audio seront connectés dans une prochaine version."
            )
        }
    }
}

@Composable
fun PrayerHome() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Heures de prière") }
        item { FeatureCard("Fajr", "Prière de l'aube") }
        item { FeatureCard("Dhuhr", "Prière de midi") }
        item { FeatureCard("Asr", "Prière de l'après-midi") }
        item { FeatureCard("Maghrib", "Prière du coucher du soleil") }
        item { FeatureCard("Isha", "Prière de la nuit") }
        item {
            FeatureCard(
                "🧭 Qibla",
                "Direction de La Mecque"
            )
        }
    }
}

@Composable
fun MoreHome() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle("Autres fonctionnalités") }
        item { FeatureCard("📿 Tasbih", "Compteur de glorifications") }
        item { FeatureCard("🤲 Invocations", "Adhkar du matin et du soir") }
        item {
            FeatureCard(
                "📜 Histoires des prophètes",
                "Découvrir les prophètes"
            )
        }
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
        color = Green
    )
}

@Composable
fun FeatureCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
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
