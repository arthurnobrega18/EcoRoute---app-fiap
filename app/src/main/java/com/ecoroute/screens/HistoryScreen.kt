package com.ecoroute.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoroute.data.history.HistoryRepository
import com.ecoroute.domain.ComparisonRecord
import com.ecoroute.ui.components.BottomNavBar
import com.ecoroute.ui.components.BottomTab
import com.ecoroute.ui.components.EmptyState
import com.ecoroute.ui.icons.IconHistory
import com.ecoroute.ui.theme.AppBackground
import com.ecoroute.ui.theme.AppBorder
import com.ecoroute.ui.theme.AppGreenTint
import com.ecoroute.ui.theme.AppInk
import com.ecoroute.ui.theme.AppInkMuted
import com.ecoroute.ui.theme.AppSurface
import com.ecoroute.ui.theme.EcoGreen40
import com.ecoroute.ui.theme.TransportModeIcon
import com.ecoroute.ui.theme.emissionTier
import com.ecoroute.ui.theme.label
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    onHome: () -> Unit,
    onInsights: () -> Unit,
    onNewRoute: () -> Unit,
) {
    val context = LocalContext.current
    val historyRepository = remember { HistoryRepository(context) }
    val history by historyRepository.observeHistory().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                active = BottomTab.HISTORY,
                onHome = onHome,
                onHistory = {},
                onInsights = onInsights,
            )
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(innerPadding),
                iconTint = EcoGreen40,
                iconBackground = AppGreenTint,
                title = "Nenhuma comparação salva ainda",
                subtitle = "Suas comparações de rota aparecerão aqui assim que você salvar a primeira.",
                ctaLabel = "Nova comparação de rota",
                onCtaClick = onNewRoute,
                icon = { tint -> IconHistory(tint = tint, size = 38.dp) },
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .padding(top = 28.dp),
        ) {
            Text(
                text = "Histórico",
                color = AppInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 18.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(history) { record -> ComparisonRecordCard(record) }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@Composable
private fun ComparisonRecordCard(record: ComparisonRecord) {
    val best = record.estimates.minByOrNull { it.emissionGrams }
    val worst = record.estimates.maxByOrNull { it.emissionGrams }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(16.dp))
            .padding(15.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${record.origin} → ${record.destination}",
                color = AppInk,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )
            Text(
                text = dateFormat.format(Date(record.timestampMillis)),
                color = AppInkMuted,
                fontSize = 11.sp,
            )
        }
        if (best != null) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val tier = best.mode.emissionTier
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tier.tint),
                    contentAlignment = Alignment.Center,
                ) {
                    TransportModeIcon(mode = best.mode, tint = tier.color, size = 14.dp)
                }
                Text(
                    text = buildAnnotatedModeLabel(best.mode.label, "${best.emissionGrams.roundToInt()} g CO2"),
                    color = AppInkMuted,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                if (worst != null && worst.emissionGrams > best.emissionGrams) {
                    Text(
                        text = "evitou ${(worst.emissionGrams - best.emissionGrams).roundToInt()} g vs. ${worst.mode.label.lowercase()}",
                        color = AppInkMuted,
                        fontSize = 11.5.sp,
                    )
                }
            }
        }
    }
}

private fun buildAnnotatedModeLabel(mode: String, co2: String) = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AppInk)) {
        append(mode)
    }
    append(" · $co2")
}
