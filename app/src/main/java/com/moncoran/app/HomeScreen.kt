package com.moncoran.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onCoran: () -> Unit,
    onAudio: () -> Unit,
    onPrayer: () -> Unit,
    onQibla: () -> Unit,
    onTasbih: () -> Unit,
    onInvocations: () -> Unit,
    onMore: () -> Unit
) {
    val items = listOf(
        Triple("📖", "Coran", onCoran),
        Triple("🎧", "Audio", onAudio),
        Triple("🕌", "Prières", onPrayer),
        Triple("🧭", "Qibla", onQibla),
        Triple("📿", "Tasbih", onTasbih),
        Triple("🤲", "Invocations", onInvocations),
        Triple("☪️", "Plus", onMore)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Accueil", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green)
        Spacer(modifier = Modifier.height(12.dp))
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (icon, title, action) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(6.dp)
                            .clickable { action() },
                        colors = CardDefaults.cardColors(containerColor = CardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green)
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f).padding(6.dp))
            }
        }
    }
}
