package com.moncoran.app

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Prophet(
    val arabic: String,
    @StringRes val name: Int,
    @StringRes val story: Int,
    @StringRes val source: Int
)

// Récits résumés. À faire relire par une source de confiance avant publication.
val prophets = listOf(
    Prophet("آدَمُ", R.string.prophet_adam_name, R.string.prophet_adam_story, R.string.prophet_adam_source),
    Prophet("نُوحٌ", R.string.prophet_nouh_name, R.string.prophet_nouh_story, R.string.prophet_nouh_source),
    Prophet("إِبْرَاهِيمُ", R.string.prophet_ibrahim_name, R.string.prophet_ibrahim_story, R.string.prophet_ibrahim_source),
    Prophet("يُوسُفُ", R.string.prophet_youssouf_name, R.string.prophet_youssouf_story, R.string.prophet_youssouf_source),
    Prophet("مُوسَى", R.string.prophet_moussa_name, R.string.prophet_moussa_story, R.string.prophet_moussa_source),
    Prophet("أَيُّوبُ", R.string.prophet_ayyoub_name, R.string.prophet_ayyoub_story, R.string.prophet_ayyoub_source),
    Prophet("يُونُسُ", R.string.prophet_younes_name, R.string.prophet_younes_story, R.string.prophet_younes_source),
    Prophet("عِيسَى", R.string.prophet_issa_name, R.string.prophet_issa_story, R.string.prophet_issa_source),
    Prophet("مُحَمَّدٌ ﷺ", R.string.prophet_muhammad_name, R.string.prophet_muhammad_story, R.string.prophet_muhammad_source)
)

@Composable
fun ProphetsScreen(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(-1) }

    if (selected >= 0) {
        ProphetDetail(prophets[selected], onBack = { selected = -1 })
        return
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back), color = Green)
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.prophets_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            itemsIndexed(prophets) { index, prophet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = index },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(prophet.name),
                            color = Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            prophet.arabic,
                            color = Green,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProphetDetail(prophet: Prophet, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.back), color = Green)
        }
        Text(
            prophet.arabic,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Green,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(prophet.name),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Text(stringResource(prophet.story), fontSize = 16.sp, lineHeight = 24.sp)
        Text(
            stringResource(R.string.references, stringResource(prophet.source)),
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
