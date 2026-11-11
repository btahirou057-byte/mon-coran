package com.moncoran.app

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Prophet(
    val arabic: String,
    val name: String,
    val story: String,
    val source: String
)

// Récits résumés. À faire relire par une source de confiance avant publication.
val prophets = listOf(
    Prophet(
        "آدَمُ",
        "Adam (عليه السلام)",
        "Adam est le premier homme et le premier prophète. Allah l'a créé d'argile et a ordonné aux anges de se prosterner devant lui. Tous l'ont fait, sauf Iblis, par orgueil.\n\n" +
            "Adam et son épouse vécurent au Paradis, avec l'interdiction de s'approcher d'un arbre. Satan les trompa et ils en mangèrent. Ils se repentirent aussitôt, Allah leur pardonna et les envoya sur terre, où commence l'histoire de l'humanité.\n\n" +
            "Leçon : le repentir sincère est toujours ouvert, et l'orgueil est la source de la perdition d'Iblis.",
        "Sourate Al-Baqara (30-39), Sourate Al-A'raf (11-25)"
    ),
    Prophet(
        "نُوحٌ",
        "Nouh (عليه السلام)",
        "Nouh appela son peuple à adorer Allah seul pendant des centaines d'années. Peu de gens crurent, et la plupart se moquèrent de lui.\n\n" +
            "Allah lui ordonna de construire une arche. Quand le déluge arriva, Nouh y embarqua les croyants et un couple de chaque espèce. Les mécréants furent noyés, y compris l'un de ses fils qui avait refusé de croire.\n\n" +
            "Leçon : la patience dans l'appel à Allah, et la foi passe avant les liens du sang.",
        "Sourate Hud (25-48), Sourate Nouh"
    ),
    Prophet(
        "إِبْرَاهِيمُ",
        "Ibrahim (عليه السلام)",
        "Ibrahim refusa d'adorer les idoles de son peuple et leur démontra leur impuissance. Ils le jetèrent dans un grand feu, mais Allah ordonna au feu d'être fraîcheur et paix pour lui.\n\n" +
            "Il laissa son épouse Hajar et son fils Ismaïl à La Mecque, par ordre d'Allah, et la source de Zamzam jaillit. Plus tard, il reçut en songe l'ordre de sacrifier son fils. Tous deux se soumirent, et Allah le racheta par un grand sacrifice. Ibrahim et Ismaïl élevèrent ensuite la Kaaba.\n\n" +
            "Leçon : la soumission totale à Allah et la confiance en Lui.",
        "Sourate Al-Anbiya (51-70), Sourate As-Saffat (99-111), Sourate Al-Baqara (125-129)"
    ),
    Prophet(
        "يُوسُفُ",
        "Youssouf (عليه السلام)",
        "Enfant, Youssouf vit en songe onze étoiles, le soleil et la lune se prosterner devant lui. Par jalousie, ses frères le jetèrent dans un puits. Il fut recueilli par une caravane et vendu en Égypte.\n\n" +
            "Il résista à la tentation de la femme de son maître, fut injustement emprisonné, puis interpréta le rêve du roi. Il devint responsable des réserves du pays. Des années plus tard, il retrouva ses frères, leur pardonna, et sa famille le rejoignit.\n\n" +
            "Leçon : la patience, la chasteté et le pardon.",
        "Sourate Youssouf (entière)"
    ),
    Prophet(
        "مُوسَى",
        "Moussa (عليه السلام)",
        "Moussa naquit alors que Pharaon faisait tuer les garçons des Bani Israël. Sa mère, par inspiration d'Allah, le mit dans un panier sur le fleuve, et il fut élevé dans le palais de Pharaon.\n\n" +
            "Il dut fuir en Madyan, puis Allah lui parla et l'envoya vers Pharaon avec son frère Haroun. Malgré les signes évidents, Pharaon refusa. Moussa conduisit son peuple, la mer s'ouvrit pour eux, et Pharaon et son armée furent engloutis.\n\n" +
            "Leçon : Allah soutient les opprimés et le tyran finit toujours par tomber.",
        "Sourate Ta-Ha (9-98), Sourate Al-Qasas, Sourate Ash-Shu'ara (10-68)"
    ),
    Prophet(
        "أَيُّوبُ",
        "Ayyoub (عليه السلام)",
        "Ayyoub fut éprouvé dans ses biens, sa famille et sa santé. Malgré une longue maladie, il resta patient et ne cessa de se tourner vers Allah. Il invoqua son Seigneur avec humilité, et Allah le guérit et lui rendit sa famille.\n\n" +
            "Leçon : la patience dans l'épreuve et l'espoir en la miséricorde d'Allah.",
        "Sourate Al-Anbiya (83-84), Sourate Sad (41-44)"
    ),
    Prophet(
        "يُونُسُ",
        "Younès (عليه السلام)",
        "Younès quitta son peuple avant qu'Allah ne l'y autorise. Il fut avalé par une grande baleine. Dans les ténèbres, il invoqua Allah :\n\n" +
            "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ\n\n" +
            "Allah l'exauça et le sauva. Son peuple finit par croire.\n\n" +
            "Leçon : se repentir et invoquer Allah, même dans les pires moments.",
        "Sourate Al-Anbiya (87-88), Sourate As-Saffat (139-148)"
    ),
    Prophet(
        "عِيسَى",
        "Issa (عليه السلام)",
        "Issa, fils de Maryam, naquit miraculeusement sans père. Dès le berceau, il parla pour défendre l'innocence de sa mère. Allah lui accorda des miracles, par Sa permission : guérir l'aveugle et le lépreux, et redonner la vie aux morts.\n\n" +
            "Il appela son peuple à adorer Allah seul. Selon le Coran, il n'a été ni tué ni crucifié : Allah l'a élevé auprès de Lui.\n\n" +
            "Leçon : Issa est un serviteur et un messager d'Allah.",
        "Sourate Maryam (16-36), Sourate Al-Ma'ida (110-118), Sourate An-Nisa (157-158)"
    ),
    Prophet(
        "مُحَمَّدٌ ﷺ",
        "Muhammad ﷺ",
        "Muhammad ﷺ naquit à La Mecque. Orphelin tôt, il était connu pour son honnêteté. À quarante ans, dans la grotte de Hira, l'ange Jibril lui apporta les premiers versets du Coran.\n\n" +
            "Après des années de persécution, il émigra à Médine (l'Hégire) et y fonda la première communauté musulmane. Il est le dernier des prophètes, envoyé comme miséricorde pour l'humanité entière.\n\n" +
            "Leçon : suivre son exemple en caractère et en adoration.",
        "Sourate Al-Alaq (1-5), Sourate Al-Ahzab (40), Sourate Al-Anbiya (107)"
    )
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
                Text("‹ Retour", color = Green)
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Histoires des prophètes",
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            prophet.name,
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
            Text("‹ Retour", color = Green)
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
            prophet.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Text(prophet.story, fontSize = 16.sp, lineHeight = 24.sp)
        Text(
            "Références : ${prophet.source}",
            fontSize = 13.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
