package com.moncoran.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Feature(val emoji: String, val titre: String, val sousTitre: String, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    onTasbih: () -> Unit,
    onInvocations: () -> Unit,
    onProphets: () -> Unit,
    onCalendar: () -> Unit,
    onShare: () -> Unit,
    onDarkMode: () -> Unit,
    onLanguage: () -> Unit
) {
    val features = listOf(
        Feature("📿", "Tasbih", "Compteur de glorifications", onTasbih),
        Feature("🤲", "Invocations", "Adhkar du matin et du soir", onInvocations),
        Feature("📜", "Histoires des prophètes", "Découvrir les prophètes", onProphets),
        Feature("📅", "Calendrier musulman", "Date hégirienne et fêtes", onCalendar),
        Feature("📤", "Partager l'application", "Envoyer à un ami", onShare),
        Feature("🌙", "Mode sombre", "Personnaliser l'affichage", onDarkMode),
        Feature("🌍", "Langue", "Français • English • 中文 • العربية", onLanguage)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.coran_accueil),
                    contentDescription = "Le Saint Coran",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(32.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text("Coran", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Votre compagnon spirituel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        items(features) { f ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { f.onClick() },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("${f.emoji}  ${f.titre}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(f.sousTitre, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
