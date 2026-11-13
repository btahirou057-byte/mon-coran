package com.moncoran.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PrayerGold = Color(0xFFD6AE58)

private fun computeTimes(lat: Double, lon: Double, date: Date): PrayerTimes {
    return PrayerTimes(
        Coordinates(lat, lon),
        DateComponents.from(date),
        CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters()
    )
}

private fun fmtTime(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

private suspend fun findCity(query: String): PrayerCity? =
    withContext(Dispatchers.IO) {
        try {
            val q = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1"
            val r = org.json.JSONObject(java.net.URL(url).readText())
                .getJSONArray("results").getJSONObject(0)
            PrayerCity(
                r.getString("name") + ", " + r.optString("country"),
                r.getDouble("latitude"),
                r.getDouble("longitude")
            )
        } catch (e: Exception) {
            null
        }
    }

private fun lastLocation(context: Context): android.location.Location? {
    val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return try {
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstNotNullOfOrNull { p ->
                try {
                    if (manager.isProviderEnabled(p)) manager.getLastKnownLocation(p) else null
                } catch (_: SecurityException) {
                    null
                }
            }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun PrayerHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var latitude by remember { mutableDoubleStateOf(9.6412) }
    var longitude by remember { mutableDoubleStateOf(-13.5784) }
    var locationName by remember { mutableStateOf("Conakry") }
    var message by remember { mutableStateOf("") }
    var cityQuery by remember { mutableStateOf("") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun useGps() {
        val loc = lastLocation(context)
        if (loc != null) {
            latitude = loc.latitude
            longitude = loc.longitude
            locationName = tr("Position GPS", "GPS location", "GPS位置", "موقع GPS")
            message = tr(
                "Position détectée avec succès.",
                "Location detected successfully.",
                "已成功获取位置。",
                "تم تحديد الموقع بنجاح."
            )
        } else {
            message = tr(
                "Active le GPS de ton téléphone puis réessaie.",
                "Turn on your phone's GPS and try again.",
                "请开启手机GPS后重试。",
                "فعّل GPS في هاتفك ثم أعد المحاولة."
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted =
            perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            useGps()
        } else {
            message = tr(
                "Autorisation GPS refusée. Choisis une ville manuellement.",
                "GPS permission denied. Choose a city manually.",
                "GPS权限被拒绝。请手动选择城市。",
                "تم رفض إذن GPS. اختر مدينة يدويًا."
            )
        }
    }

    val dayKey = remember(now) {
        val c = Calendar.getInstance()
        c.timeInMillis = now
        c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    val times = remember(latitude, longitude, dayKey) {
        runCatching { computeTimes(latitude, longitude, Date(now)) }.getOrNull()
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            tr("Heures de prière", "Prayer times", "礼拜时间", "مواقيت الصلاة"),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Green
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            tr("Lieu : ", "Location: ", "地点：", "الموقع: ") + locationName,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cityQuery,
            onValueChange = { cityQuery = it },
            label = {
                Text(tr("Ville (monde entier)", "City (worldwide)", "城市（全球）", "مدينة (العالم)"))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (cityQuery.isNotBlank()) {
                    scope.launch {
                        val found = findCity(cityQuery)
                        if (found != null) {
                            latitude = found.latitude
                            longitude = found.longitude
                            locationName = found.name
                            message = ""
                        } else {
                            message = tr(
                                "Ville introuvable. Vérifie ta connexion.",
                                "City not found. Check your connection.",
                                "未找到城市。请检查网络。",
                                "المدينة غير موجودة. تحقق من الاتصال."
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(tr("Rechercher", "Search", "搜索", "بحث"))
        }

        OutlinedButton(
            onClick = {
                val fine = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (fine || coarse) {
                    useGps()
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

        if (message.isNotEmpty()) {
            Text(message, color = Green, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (times != null) {
            val list = listOf(
                tr("Fajr", "Fajr", "晨礼", "الفجر") to times.fajr,
                tr("Dhuhr", "Dhuhr", "晌礼", "الظهر") to times.dhuhr,
                tr("Asr", "Asr", "晡礼", "العصر") to times.asr,
                tr("Maghrib", "Maghrib", "昏礼", "المغرب") to times.maghrib,
                tr("Isha", "Isha", "宵礼", "العشاء") to times.isha
            )

            val next = list.firstOrNull { it.second.time > now }

            val tomorrowFajr = remember(latitude, longitude, dayKey) {
                val t = Calendar.getInstance()
                t.timeInMillis = now
                t.add(Calendar.DAY_OF_YEAR, 1)
                computeTimes(latitude, longitude, t.time).fajr
            }

            val nextName = next?.first ?: tr("Fajr", "Fajr", "晨礼", "الفجر")
            val nextDate = next?.second ?: tomorrowFajr
            val remaining = (nextDate.time - now).coerceAtLeast(0)
            val h = remaining / 3600000
            val m = (remaining / 60000) % 60
            val s = (remaining / 1000) % 60

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Green),
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
                        "%02d:%02d:%02d".format(h, m, s),
                        color = PrayerGold,
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
                color = Green
            )

            list.forEach { (name, time) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Green)
                        Text(fmtTime(time), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
