package com.ecoroute.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoroute.data.history.HistoryInsights
import com.ecoroute.data.history.HistoryRepository
import com.ecoroute.domain.ComparisonRecord
import com.ecoroute.ui.components.BottomNavBar
import com.ecoroute.ui.components.BottomTab
import com.ecoroute.ui.components.EmptyState
import com.ecoroute.ui.icons.IconRouteEmpty
import com.ecoroute.ui.theme.AppBackground
import com.ecoroute.ui.theme.AppBorder
import com.ecoroute.ui.theme.AppGreenTint
import com.ecoroute.ui.theme.AppInk
import com.ecoroute.ui.theme.AppInkMuted
import com.ecoroute.ui.theme.AppSurface
import com.ecoroute.ui.theme.EcoGreen40
import com.ecoroute.ui.theme.TransportModeIcon
import com.ecoroute.ui.theme.label
import kotlin.math.roundToInt

@Composable
fun InsightsScreen(
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onNewRoute: () -> Unit,
) {
    val context = LocalContext.current
    val historyRepository = remember { HistoryRepository(context) }
    val insights by historyRepository.observeInsights().collectAsState(
        initial = HistoryInsights(0, 0.0, null, 0)
    )
    val history by historyRepository.observeHistory().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                active = BottomTab.INSIGHTS,
                onHome = onHome,
                onHistory = onHistory,
                onInsights = {},
            )
        }
    ) { innerPadding ->
        if (insights.totalComparisons == 0) {
            EmptyState(
                modifier = Modifier.padding(innerPadding),
                iconTint = EcoGreen40,
                iconBackground = AppGreenTint,
                title = "Nenhuma comparação salva ainda",
                subtitle = "Compare uma rota para começar a ver o impacto ambiental das suas escolhas.",
                ctaLabel = "Nova comparação de rota",
                onCtaClick = onNewRoute,
                icon = { tint -> IconRouteEmpty(tint = tint) },
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .padding(top = 28.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = "Insights", color = AppInk, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(modifier = Modifier.weight(1f), label = "Comparações", value = "${insights.totalComparisons}", valueColor = AppInk)
                StatTile(
                    modifier = Modifier.weight(1f),
                    label = "CO2 evitado",
                    value = "${"%.1f".format(insights.totalCo2AvoidedGrams / 1000)} kg",
                    valueColor = EcoGreen40,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppSurface)
                    .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Text(
                    text = "CO2 evitado por comparação",
                    color = AppInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                AvoidedCo2Chart(history = history.takeLast(6))
            }

            Spacer(modifier = Modifier.height(16.dp))

            insights.mostSustainableMode?.let { mode ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppSurface)
                        .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(18.dp))
                        .padding(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(AppGreenTint),
                            contentAlignment = Alignment.Center,
                        ) {
                            TransportModeIcon(mode = mode, tint = EcoGreen40, size = 20.dp)
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(text = "Modal mais sustentável", color = AppInk, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${mode.label} venceu em ${insights.mostSustainableModeCount} de ${insights.totalComparisons} rotas",
                                color = AppInkMuted,
                                fontSize = 12.5.sp,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEEECE3)),
                    ) {
                        val fraction = (insights.mostSustainableModeCount.toFloat() / insights.totalComparisons)
                            .coerceIn(0.04f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxSize()
                                .background(EcoGreen40),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(text = label, color = AppInkMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun AvoidedCo2Chart(history: List<ComparisonRecord>) {
    if (history.isEmpty()) return

    val avoidedPerRecord = history.map { record ->
        if (record.estimates.isEmpty()) 0.0
        else {
            val best = record.estimates.minOf { it.emissionGrams }
            val worst = record.estimates.maxOf { it.emissionGrams }
            worst - best
        }
    }
    val maxValue = (avoidedPerRecord.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        avoidedPerRecord.forEachIndexed { index, value ->
            val isLast = index == avoidedPerRecord.lastIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction = (value / maxValue).toFloat().coerceIn(0.08f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isLast) EcoGreen40 else Color(0xFFCFE6D8)),
            )
        }
    }
}
