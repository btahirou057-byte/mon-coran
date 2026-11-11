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
private val ScreenGreen = Color(0xFF075B46)
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
                    locationName = "Position GPS"
                    locationMessage = "Position détectée avec succès."
                } else {
                    locationMessage =
                        "Position introuvable. Active le GPS et réessaie."
                }
            } else {
                locationMessage =
                    "Autorisation GPS refusée. Choisis une ville manuellement."
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
            "Heures de prière",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = ScreenGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Lieu : $locationName",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { menuExpanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choisir une ville")
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
                        locationName = "Position GPS"
                        locationMessage = "Position détectée avec succès."
                    } else {
                        locationMessage =
                            "Active le GPS de ton téléphone puis réessaie."
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
            Text("📍 Détecter ma position GPS")
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
                "Fajr" to prayerTimes.fajr,
                "Dhuhr" to prayerTimes.dhuhr,
                "Asr" to prayerTimes.asr,
                "Maghrib" to prayerTimes.maghrib,
                "Isha" to prayerTimes.isha
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

            val nextName = nextPrayer?.first ?: "Fajr"
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
                        "Prochaine prière : $nextName",
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
                        "Temps restant",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Les cinq prières",
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
                        containerColor = Color.White
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
                "Méthode de calcul : Muslim World League.",
                modifier = Modifier.padding(top = 12.dp),
                color = Color.DarkGray,
                fontSize = 12.sp
            )
        } else {
            Text(
                "Impossible de calculer les horaires. Vérifie les coordonnées.",
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
fun MoreHome(onTasbih: () -> Unit = {}, onInvocations: () -> Unit = {}) {
        var showProphets by remember { mutableStateOf(false) }
    if (showProphets) {
        ProphetsScreen(onBack = { showProphets = false })
        return
    }
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item { SectionTitle("Autres fonctionnalités") }
            item { FeatureCard("📿 Tasbih", "Compteur de glorifications", onTasbih) }
    item { FeatureCard("🤲 Invocations", "Adhkar du matin et du soir", onInvocations) }
    item { FeatureCard("📜 Histoires des prophètes", "Découvrir les prophètes", { showProphets = true }) }
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
fun FeatureCard(title: String, description: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
                        .padding(bottom = 10.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
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
                color = Color.DarkGray
            )
        }
    }
}
@Composable
fun QiblaScreen(latitude: Double = 9.6412, longitude: Double = -13.5784) {
    val context = LocalContext.current
    var azimuth by remember { mutableStateOf(0f) }
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
        Text("Qibla", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Direction : ${qibla.toInt()}° depuis le Nord")
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
        Text("Tiens le téléphone à plat. La Kaaba 🕋 indique la direction de la Mecque.")
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
                Text("‹ Retour", color = ScreenGreen)
            }
        }
        Text("Tasbih", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ScreenGreen)
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
            Text("Terminé ✅ Qu'Allah accepte", fontSize = 18.sp, color = ScreenGreen)
        } else {
            Text("Appuie sur le cercle pour compter")
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { count = 0 }) { Text("Remise à zéro") }
    }
}
