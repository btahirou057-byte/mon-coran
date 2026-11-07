package com.moncoran.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerBar(
    reciterLabel: String, reciterNames: List<String>, statusText: String,
    isPlaying: Boolean, isPreparing: Boolean, repeatOne: Boolean,
    speedLabel: String, menuOpen: Boolean, onMenuOpen: (Boolean) -> Unit,
    onPickReciter: (Int) -> Unit, onRepeat: () -> Unit, onPrev: () -> Unit,
    onToggle: () -> Unit, onNext: () -> Unit, onSpeed: () -> Unit,
    onStop: () -> Unit
) {
    Surface(color = Color(0xFF111111), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Text(
                    "🎙 $reciterLabel ▾", color = Color.White, fontSize = 14.sp,
                    modifier = Modifier.clickable { onMenuOpen(true) }.padding(6.dp)
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { onMenuOpen(false) }) {
                    reciterNames.forEachIndexed { i, n ->
                        DropdownMenuItem(text = { Text(n) }, onClick = { onPickReciter(i) })
                    }
                }
            }
            Text(statusText, color = Color.LightGray, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                TextButton(onClick = onRepeat) {
                    Text("🔁", fontSize = 22.sp, color = if (repeatOne) Gold else Color.Gray)
                }
                TextButton(onClick = onPrev) { Text("⏮", fontSize = 22.sp, color = Color.White) }
                Button(
                    onClick = onToggle,
                    enabled = !isPreparing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Green,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Green
                    )
                ) {
                    Text(if (isPreparing) "…" else if (isPlaying) "⏸" else "▶", fontSize = 20.sp)
                }
                TextButton(onClick = onNext) { Text("⏭", fontSize = 22.sp, color = Color.White) }
                TextButton(onClick = onSpeed) { Text(speedLabel, color = Color.White) }
                TextButton(onClick = onStop) { Text("⏹", fontSize = 22.sp, color = Color.White) }
            }
        }
    }
}
