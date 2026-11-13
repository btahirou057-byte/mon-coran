package com.moncoran.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ISLAMIC_EPOCH = 1948439.5

private fun islamicToJd(year: Int, month: Int, day: Int): Double =
    day + Math.ceil(29.5 * (month - 1)) + (year - 1) * 354 +
        Math.floor((3 + 11.0 * year) / 30) + ISLAMIC_EPOCH - 1

private fun jdToIslamic(jdIn: Double): Triple<Int, Int, Int> {
    val jd = Math.floor(jdIn) + 0.5
    val year = Math.floor((30 * (jd - ISLAMIC_EPOCH) + 10646) / 10631).toInt()
    val month = minOf(12, Math.ceil((jd - (29 + islamicToJd(year, 1, 1))) / 29.5).toInt() + 1)
    val day = (jd - islamicToJd(year, month, 1)).toInt() + 1
    return Triple(year, month, day)
}

private fun gregorianToJd(year: Int, month: Int, day: Int): Double {
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    return jdn - 0.5
}

private val monthsFr = listOf(
    "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani", "Joumada al-oula", "Joumada ath-thania",
    "Rajab", "Cha'bane", "Ramadan", "Chawwal", "Dhou al-qi'da", "Dhou al-hijja"
)
private val monthsEn = listOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
    "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
)
private val monthsZh = listOf(
    "穆哈兰姆月", "色法尔月", "赖比尔·敖外鲁月", "赖比尔·阿色尼月", "主马达·敖外鲁月", "主马达·阿色尼月",
    "赖哲卜月", "舍尔邦月", "斋月", "闪瓦鲁月", "都尔喀尔德月", "都尔黑哲月"
)
private val monthsAr = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

private fun monthName(m: Int): String {
    val list = when (appLang) {
        Lang.FR -> monthsFr
        Lang.EN -> monthsEn
        Lang.ZH -> monthsZh
        Lang.AR -> monthsAr
    }
    return list[m - 1]
}

private fun weekdayLetters(): List<String> = when (appLang) {
    Lang.FR -> listOf("D", "L", "M", "M", "J", "V", "S")
    Lang.EN -> listOf("S", "M", "T", "W", "T", "F", "S")
    Lang.ZH -> listOf("日", "一", "二", "三", "四", "五", "六")
    Lang.AR -> listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")
}

private fun eventName(month: Int, day: Int): String? = when {
    month == 1 && day == 1 -> tr("Nouvel an hégirien", "Islamic New Year", "伊斯兰历新年", "رأس السنة الهجرية")
    month == 1 && day == 10 -> tr("Achoura", "Ashura", "阿舒拉日", "عاشوراء")
    month == 9 && day == 1 -> tr("Début du Ramadan", "Start of Ramadan", "斋月开始", "بداية رمضان")
    month == 10 && day == 1 -> tr("Aïd al-Fitr", "Eid al-Fitr", "开斋节", "عيد الفطر")
    month == 12 && day == 9 -> tr("Jour d'Arafat", "Day of Arafah", "阿拉法特日", "يوم عرفة")
    month == 12 && day == 10 -> tr("Aïd al-Adha", "Eid al-Adha", "宰牲节", "عيد الأضحى")
    else -> null
}

@Composable
fun HijriCalendarScreen(onBack: () -> Unit) {
    var offset by rememberSaveable { mutableIntStateOf(0) }

    val now = Calendar.getInstance()
    val todayJd = gregorianToJd(
        now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH)
    )
    val (todayYear, todayMonth, todayDay) = jdToIslamic(todayJd)

    val total = todayYear * 12 + (todayMonth - 1) + offset
    val shownYear = total / 12
    val shownMonth = total % 12 + 1
    val firstJd = islamicToJd(shownYear, shownMonth, 1)
    val nextJd = if (shownMonth == 12) islamicToJd(shownYear + 1, 1, 1)
    else islamicToJd(shownYear, shownMonth + 1, 1)
    val daysInMonth = (nextJd - firstJd).toInt()
    val startWeekday = (Math.floor(firstJd + 1.5).toLong() % 7).toInt()

    val locale = when (appLang) {
        Lang.FR -> Locale("fr")
        Lang.EN -> Locale.ENGLISH
        Lang.ZH -> Locale.CHINESE
        Lang.AR -> Locale("ar")
    }
    val gregorian = SimpleDateFormat("EEEE d MMMM yyyy", locale).format(now.time)

    val cells = List(startWeekday) { 0 } + (1..daysInMonth).toList()
    val padded = cells + List((7 - cells.size % 7) % 7) { 0 }

    val events = (1..daysInMonth).mapNotNull { d -> eventName(shownMonth, d)?.let { d to it } }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(tr("‹ Retour", "‹ Back", "‹ 返回", "‹ رجوع"), color = Green)
        }
        Text(
            tr("Calendrier musulman", "Islamic calendar", "伊斯兰历", "التقويم الهجري"),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Green),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$todayDay ${monthName(todayMonth)} $todayYear",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$todayDay ${monthsAr[todayMonth - 1]} $todayYear هـ",
                    color = Gold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(gregorian, color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { offset-- }) { Text("‹", fontSize = 24.sp, color = Green) }
            Text(
                "${monthName(shownMonth)} $shownYear",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { offset++ }) { Text("›", fontSize = 24.sp, color = Green) }
        }
        if (offset != 0) {
            TextButton(onClick = { offset = 0 }) {
                Text(tr("Aujourd'hui", "Today", "今天", "اليوم"), color = Green)
            }
        }

        Row(Modifier.fillMaxWidth()) {
            weekdayLetters().forEach { letter ->
                Text(
                    letter,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        padded.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { d ->
                    val isToday = offset == 0 && d == todayDay
                    val hasEvent = d != 0 && eventName(shownMonth, d) != null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isToday -> Green
                                    hasEvent -> Gold.copy(alpha = 0.35f)
                                    else -> Color.Transparent
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (d != 0) {
                            Text(
                                "$d",
                                color = if (isToday) Color.White else TextPrimary,
                                fontWeight = if (hasEvent || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        if (events.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                tr("Occasions du mois", "This month's occasions", "本月节日", "مناسبات الشهر"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
            events.forEach { (d, name) ->
                Text(
                    "• $d ${monthName(shownMonth)} : $name",
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        Text(
            tr(
                "Dates calculées. Elles peuvent varier d'un jour selon l'observation de la lune.",
                "Calculated dates. They may vary by one day depending on moon sighting.",
                "日期为推算值，可能因月亮观测而相差一天。",
                "التواريخ محسوبة وقد تختلف بيوم حسب رؤية الهلال."
            ),
            modifier = Modifier.padding(top = 16.dp),
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
