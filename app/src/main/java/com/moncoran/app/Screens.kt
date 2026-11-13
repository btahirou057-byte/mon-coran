package com.moncoran.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.PrayerTimes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import com.batoulapps.adhan.Qibla
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

private val ScreenGreen: Color get() = Green
private val ScreenGold = Color(0xFFD6AE58)

data class PrayerCity(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private val cities = listOf(
    PrayerCity("Conakry", 9.6412, -13.5784),
    PrayerCity("Labé", 11.3182, -12.2833),
    PrayerCity("Kindia", 10.0569, -12.8658),
    PrayerCity("Kankan", 10.3854, -9.3057),
    PrayerCity("Nzérékoré", 7.7562, -8.8179),
    PrayerCity("Dakar", 14.7167, -17.4677)
)

private fun getPrayerTimes(
    latitude: Double,
    longitude: Double,
    date: Date
): PrayerTimes {
    val coordinates = Coordinates(latitude, longitude)
    val dateComponents = DateComponents.from(date)
    val parameters =
        CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters()

    return PrayerTimes(coordinates, dateComponents, parameters)
}

private fun formatPrayerTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}

@Composable
fun PrayerHome() {
    val context = LocalContext.current

    var selectedCity by remember { mutableStateOf(cities[0]) }
    var latitude by remember { mutableDoubleStateOf(cities[0].latitude) }
    var longitude by remember { mutableDoubleStateOf(cities[0].longitude) }
    var locationName by remember { mutableStateOf("Conakry") }
    var menuExpanded by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf("") }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                val location = getDeviceLocation(context)

                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    locationName = tr("Position GPS", "GPS location", "GPS位置", "موقع GPS")
                    locationMessage = tr(
                        "Position détectée avec succès.",
                        "Location detected successfully.",
                        "已成功获取位置。",
                        "تم تحديد الموقع بنجاح."
                    )
                } else {
                    locationMessage = tr(
                        "Position introuvable. Active le GPS et réessaie.",
                        "Location not found. Turn on GPS and try again.",
                        "未找到位置。请开启GPS后重试。",
                        "تعذّر تحديد الموقع. فعّل GPS وأعد المحاولة."
                    )
                }
            } else {
                locationMessage = tr(
                    "Autorisation GPS refusée. Choisis une ville manuellement.",
                    "GPS permission denied. Choose a city manually.",
                    "GPS权限被拒绝。请手动选择城市。",
                    "تم رفض إذن GPS. اختر مدينة يدويًا."
                )
            }
        }

    val dayKey = remember(currentTime) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        calendar.get(Calendar.YEAR) * 1000 +
            calendar.get(Calendar.DAY_OF_YEAR)
    }

    val prayerTimes = remember(latitude, longitude, dayKey) {
        runCatching {
            getPrayerTimes(latitude, longitude, Date(currentTime))
        }.getOrNull()
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            tr("Heures de prière", "Prayer times", "礼拜时间", "مواقيت الصلاة"),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = ScreenGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            tr("Lieu : ", "Location: ", "地点：", "الموقع: ") + locationName,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { menuExpanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(tr("Choisir une ville", "Choose a city", "选择城市", "اختر مدينة"))
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = {
                        selectedCity = city
                        latitude = city.latitude
                        longitude = city.longitude
                        locationName = city.name
                        locationMessage = ""
                        menuExpanded = false
                    }
                )
            }
        }

        OutlinedButton(
            onClick = {
                val fineGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                val coarseGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    val location = getDeviceLocation(context)

                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        locationName = tr("Position GPS", "GPS location", "GPS位置", "موقع GPS")
                        locationMessage = tr(
                            "Position détectée avec succès.",
                            "Location detected successfully.",
                            "已成功获取位置。",
                            "تم تحديد الموقع بنجاح."
                        )
                    } else {
                        locationMessage = tr(
                            "Active le GPS de ton téléphone puis réessaie.",
                            "Turn on your phone's GPS and try again.",
                            "请开启手机GPS后重试。",
                            "فعّل GPS في هاتفك ثم أعد المحاولة."
                        )
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                tr(
                    "📍 Détecter ma position GPS",
                    "📍 Detect my GPS location",
                    "📍 检测我的GPS位置",
                    "📍 تحديد موقعي عبر GPS"
                )
            )
        }

        if (locationMessage.isNotEmpty()) {
            Text(
                locationMessage,
                color = ScreenGreen,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (prayerTimes != null) {
            val prayerList = listOf(
                tr("Fajr", "Fajr", "晨礼", "الفجر") to prayerTimes.fajr,
                tr("Dhuhr", "Dhuhr", "晌礼", "الظهر") to prayerTimes.dhuhr,
                tr("Asr", "Asr", "晡礼", "العصر") to prayerTimes.asr,
                tr("Maghrib", "Maghrib", "昏礼", "المغرب") to prayerTimes.maghrib,
                tr("Isha", "Isha", "宵礼", "العشاء") to prayerTimes.isha
            )

            val nextPrayer = prayerList.firstOrNull {
                it.second.time > currentTime
            }

            val tomorrowFajr = remember(latitude, longitude, dayKey) {
                val tomorrow = Calendar.getInstance()
                tomorrow.timeInMillis = currentTime
                tomorrow.add(Calendar.DAY_OF_YEAR, 1)

                getPrayerTimes(
                    latitude,
                    longitude,
                    tomorrow.time
                ).fajr
            }

            val nextName = nextPrayer?.first ?: tr("Fajr", "Fajr", "晨礼", "الفجر")
            val nextDate = nextPrayer?.second ?: tomorrowFajr

            val remaining = (nextDate.time - currentTime).coerceAtLeast(0)
            val hours = remaining / 3600000
            val minutes = (remaining / 60000) % 60
            val seconds = (remaining / 1000) % 60

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ScreenGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        tr(
                            "Prochaine prière : $nextName",
                            "Next prayer: $nextName",
                            "下一次礼拜：$nextName",
                            "الصلاة القادمة: $nextName"
                        ),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "%02d:%02d:%02d".format(hours, minutes, seconds),
                        color = ScreenGold,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        tr("Temps restant", "Time remaining", "剩余时间", "الوقت المتبقي"),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                tr("Les cinq prières", "The five prayers", "五次礼拜", "الصلوات الخمس"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ScreenGreen
            )

            prayerList.forEach { (name, time) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBg
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScreenGreen
                        )

                        Text(
                            formatPrayerTime(time),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                tr(
                    "Méthode de calcul : Muslim World League.",
                    "Calculation method: Muslim World League.",
                    "计算方法：穆斯林世界联盟。",
                    "طريقة الحساب: رابطة العالم الإسلامي."
                ),
                modifier = Modifier.padding(top = 12.dp),
                color = TextSecondary,
                fontSize = 12.sp
            )
        } else {
            Text(
                tr(
                    "Impossible de calculer les horaires. Vérifie les coordonnées.",
                    "Unable to calculate prayer times. Check the coordinates.",
                    "无法计算礼拜时间。请检查坐标。",
                    "تعذّر حساب المواقيت. تحقق من الإحداثيات."
                ),
                color = Color.Red
            )
        }
    }
}

private fun getDeviceLocation(
    context: Context
): android.location.Location? {
    val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return try {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        providers.firstNotNullOfOrNull { provider ->
            try {
                if (manager.isProviderEnabled(provider)) {
                    manager.getLastKnownLocation(provider)
                } else {
                    null
                }
            } catch (_: SecurityException) {
                null
            }
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun AudioHome() {
    val pick = tr(
        "Choisis-le dans le lecteur d'une sourate",
        "Choose it in a surah's player",
        "在章节播放器中选择",
        "اخترهم من مشغّل السورة"
    )
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle(tr("Récitateurs du Coran", "Quran reciters", "古兰经诵读者", "قرّاء القرآن")) }
        item { FeatureCard("🎧 Mishary Alafasy", pick) }
        item { FeatureCard("🎧 Abdul Rahman Al-Sudais", pick) }
        item { FeatureCard("🎧 Maher Al-Muaiqly", pick) }
        item { FeatureCard("🎧 Abdul Basit", pick) }
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

@Composable
fun MoreHome(onTasbih: () -> Unit = {}, onInvocations: () -> Unit = {}) {
    val context = LocalContext.current
    var showProphets by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showLang by remember { mutableStateOf(false) }
    if (showProphets) {
        ProphetsScreen(onBack = { showProphets = false })
        return
    }
    if (showCalendar) {
        HijriCalendarScreen(onBack = { showCalendar = false })
        return
    }
    if (showLang) {
        LanguageDialog(onDismiss = { showLang = false })
    }
    val shareMessage = tr(
        "Je te recommande cette application du Saint Coran : Mon Coran 📖 (Coran, horaires de prière, Qibla, invocations).",
        "I recommend this Quran app: Mon Coran 📖 (Quran, prayer times, Qibla, supplications).",
        "推荐这款古兰经应用：Mon Coran 📖（古兰经、礼拜时间、朝向、祈祷词）。",
        "أنصحك بهذا التطبيق للقرآن الكريم: Mon Coran 📖 (القرآن، مواقيت الصلاة، القبلة، الأذكار)."
    )
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle(tr("Autres fonctionnalités", "More features", "更多功能", "ميزات أخرى")) }
        item { FeatureCard("📿 Tasbih", tr("Compteur de glorifications", "Dhikr counter", "赞念计数器", "عداد التسبيح"), onTasbih) }
        item { FeatureCard("🤲 " + tr("Invocations", "Supplications", "祈祷词", "الأذكار"), tr("Adhkar du matin et du soir", "Morning and evening adhkar", "早晚赞念词", "أذكار الصباح والمساء"), onInvocations) }
        item { FeatureCard("📜 " + tr("Histoires des prophètes", "Stories of the prophets", "先知故事", "قصص الأنبياء"), tr("Découvrir les prophètes", "Discover the prophets", "了解先知们", "تعرّف على الأنبياء"), { showProphets = true }) }
        item { FeatureCard("📅 " + tr("Calendrier musulman", "Islamic calendar", "伊斯兰历", "التقويم الهجري"), tr("Date hégirienne et fêtes", "Hijri date and holidays", "伊斯兰历日期与节日", "التاريخ الهجري والمناسبات"), { showCalendar = true }) }
        item { FeatureCard("📤 " + tr("Partager l'application", "Share the app", "分享应用", "شارك التطبيق"), tr("Envoyer à un ami", "Send to a friend", "发送给朋友", "أرسله إلى صديق"), { shareText(context, shareMessage) }) }
        item {
            FeatureCard(
                if (isDarkMode) "☀️ " + tr("Mode clair", "Light mode", "浅色模式", "الوضع الفاتح")
                else "🌙 " + tr("Mode sombre", "Dark mode", "深色模式", "الوضع الداكن"),
                tr("Personnaliser l'affichage", "Customize the display", "自定义显示", "تخصيص العرض"),
                {
                    isDarkMode = !isDarkMode
                    saveDarkMode(context, isDarkMode)
                }
            )
        }
        item { FeatureCard("🌍 " + tr("Langue", "Language", "语言", "اللغة"), "Français · English · 中文 · العربية", { showLang = true }) }
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
fun FeatureCard(title: String, description: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ScreenGreen
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun QiblaScreen(latitude: Double = 9.6412, longitude: Double = -13.5784) {
    val context = LocalContext.current
    val qibla = remember(latitude, longitude) {
        Qibla(Coordinates(latitude, longitude)).direction.toFloat()
    }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val r = FloatArray(9)
                val o = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(r, e.values)
                SensorManager.getOrientation(r, o)
                azimuth = Math.toDegrees(o[0].toDouble()).toFloat()
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(tr("Qibla", "Qibla", "朝向", "القبلة"), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            tr(
                "Direction : ${qibla.toInt()}° depuis le Nord",
                "Direction: ${qibla.toInt()}° from North",
                "方向：距正北 ${qibla.toInt()}°",
                "الاتجاه: ${qibla.toInt()}° من الشمال"
            )
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .border(4.dp, ScreenGreen, CircleShape)
                .rotate(qibla - azimuth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🕋",
                fontSize = 56.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )
            Text("▲", fontSize = 100.sp, color = ScreenGold)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            tr(
                "Tiens le téléphone à plat. La Kaaba 🕋 indique la direction de la Mecque.",
                "Hold your phone flat. The Kaaba 🕋 shows the direction of Mecca.",
                "请将手机水平放置。克尔白 🕋 指示麦加的方向。",
                "أمسك الهاتف بشكل أفقي. تشير الكعبة 🕋 إلى اتجاه مكة."
            )
        )
    }
}

@Composable
fun TasbihScreen(onBack: () -> Unit) {
    var count by remember { mutableStateOf(0) }
    var target by remember { mutableStateOf(33) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text(tr("‹ Retour", "‹ Back", "‹ 返回", "‹ رجوع"), color = ScreenGreen)
            }
        }
        Text(
            tr("Tasbih", "Tasbih", "念珠", "المسبحة"),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ScreenGreen
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(33, 99, 100).forEach { t ->
                Button(
                    onClick = { target = t; count = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target == t) ScreenGreen else Color.Gray
                    )
                ) { Text("$t") }
            }
        }
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(ScreenGreen)
                .clickable {
                    count++
                    if (count == target) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("/ $target", fontSize = 22.sp, color = ScreenGold)
            }
        }
        Spacer(Modifier.height(16.dp))
        if (count >= target) {
            Text(
                tr(
                    "Terminé ✅ Qu'Allah accepte",
                    "Done ✅ May Allah accept",
                    "完成 ✅ 愿真主接受",
                    "اكتمل ✅ تقبّل الله"
                ),
                fontSize = 18.sp,
                color = ScreenGreen
            )
        } else {
            Text(
                tr(
                    "Appuie sur le cercle pour compter",
                    "Tap the circle to count",
                    "点击圆圈开始计数",
                    "اضغط على الدائرة للعدّ"
                )
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { count = 0 }) {
            Text(tr("Remise à zéro", "Reset", "重置", "إعادة الضبط"))
        }
    }
}
