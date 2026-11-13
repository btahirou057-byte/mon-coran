package com.moncoran.app

import androidx.compose.foundation.clickable
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

@Composable
fun AudioHome() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            SectionTitle(
                tr("Récitateurs du Coran", "Quran reciters", "古兰经诵读者", "قرّاء القرآن")
            )
        }

        reciters.forEachIndexed { i, r ->
            item {
                val selected = selectedReciterIndex == i
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { selectedReciterIndex = i },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) Green else CardBg
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(17.dp)) {
                        Text(
                            "🎧 " + r.label,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else Green
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            if (selected)
                                tr("Récitateur choisi ✓", "Selected reciter ✓", "已选择 ✓", "القارئ المختار ✓")
                            else
                                tr("Appuie pour choisir", "Tap to select", "点击选择", "اضغط للاختيار"),
                            fontSize = 13.sp,
                            color = if (selected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Text(
                tr(
                    "Pour écouter, ouvre une sourate dans l'onglet Coran et appuie sur ▶.",
                    "To listen, open a surah in the Quran tab and press ▶.",
                    "要收听，请在“古兰经”标签中打开一章并点击 ▶。",
                    "للاستماع، افتح سورة من تبويب القرآن واضغط ▶."
                )
            )
        }
    }
}
