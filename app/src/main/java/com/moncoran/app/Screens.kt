package com.moncoran.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenGreen = Color(0xFF075B46)

@Composable
fun AudioHome() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle("Récitateurs du Coran") }
        item { FeatureCard("🎧 Mishary Alafasy", "Choisis-le dans le lecteur d'une sourate") }
        item { FeatureCard("🎧 Abdul Rahman Al-Sudais", "Choisis-le dans le lecteur d'une sourate") }
        item { FeatureCard("🎧 Maher Al-Muaiqly", "Choisis-le dans le lecteur d'une sourate") }
        item { FeatureCard("🎧 Abdul Basit", "Choisis-le dans le lecteur d'une sourate") }
        item { Text("Pour écouter, ouvre une sourate dans l'onglet Coran et appuie sur ▶.") }
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
        color = ScreenGreen,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun FeatureCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ScreenGreen)
            Spacer(modifier = Modifier.height(5.dp))
            Text(description, fontSize = 13.sp, color = Color.DarkGray)
        }
    }
}
