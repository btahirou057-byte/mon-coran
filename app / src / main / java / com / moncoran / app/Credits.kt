package com.moncoran.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SourceCredits() {
    val context = LocalContext.current

    fun open(url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                tr("Sources", "Sources", "来源", "المصادر"),
                color = Green,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                tr(
                    "Texte du Coran : Tanzil Project (tanzil.net)",
                    "Quran text: Tanzil Project (tanzil.net)",
                    "古兰经文本：Tanzil Project (tanzil.net)",
                    "نص القرآن: مشروع تنزيل (tanzil.net)"
                ),
                color = Green,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open("https://tanzil.net") }
                    .padding(vertical = 6.dp)
            )

            Text(
                tr(
                    "Audio et traductions : Al Quran Cloud (alquran.cloud)",
                    "Audio and translations: Al Quran Cloud (alquran.cloud)",
                    "音频和翻译：Al Quran Cloud (alquran.cloud)",
                    "الصوت والترجمات: Al Quran Cloud (alquran.cloud)"
                ),
                color = Green,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open("https://alquran.cloud") }
                    .padding(vertical = 6.dp)
            )

            Text(
                tr(
                    "Traductions : Hamidullah (FR), Pickthall (EN), Ma Jian (中文)",
                    "Translations: Hamidullah (FR), Pickthall (EN), Ma Jian (中文)",
                    "翻译：Hamidullah (法语)、Pickthall (英语)、马坚 (中文)",
                    "الترجمات: حميد الله (فرنسي)، بيكثال (إنجليزي)، ما جيان (صيني)"
                ),
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}
