package com.ecoroute.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecoroute.data.history.HistoryInsights
import com.ecoroute.data.history.HistoryRepository
import com.ecoroute.ui.components.BottomNavBar
import com.ecoroute.ui.components.BottomTab
import com.ecoroute.ui.icons.IconArrowRight
import com.ecoroute.ui.icons.IconLeaf
import com.ecoroute.ui.icons.IconPreferences
import com.ecoroute.ui.theme.AppBackground
import com.ecoroute.ui.theme.AppBlueTint
import com.ecoroute.ui.theme.AppBorder
import com.ecoroute.ui.theme.AppInk
import com.ecoroute.ui.theme.AppInkMuted
import com.ecoroute.ui.theme.AppSurface
import com.ecoroute.ui.theme.EcoBlue40
import com.ecoroute.ui.theme.EcoGreen40
import com.ecoroute.ui.theme.TransportModeIcon
import com.ecoroute.ui.theme.label

@Composable
fun HomeScreen(
    onNewRoute: () -> Unit,
    onHistory: () -> Unit,
    onInsights: () -> Unit,
    onPreferences: () -> Unit
) {
    val context = LocalContext.current
    val historyRepository = remember { HistoryRepository(context) }
    val insights by historyRepository.observeInsights().collectAsState(
        initial = HistoryInsights(0, 0.0, null, 0)
    )

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                active = BottomTab.HOME,
                onHome = {},
                onHistory = onHistory,
                onInsights = onInsights,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .padding(top = 28.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(EcoGreen40, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconLeaf(tint = AppSurface, size = 19.dp)
                    }
                    Text(
                        text = "EcoRoute",
                        color = AppInk,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onPreferences)
                        .background(AppSurface, CircleShape)
                        .border(width = 1.dp, color = AppBorder, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconPreferences(tint = AppInkMuted)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Bom te ver de novo.",
                color = AppInk,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp,
            )
            Text(
                text = "Toda rota tem uma opção mais leve para o planeta.",
                color = AppInkMuted,
                fontSize = 14.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(listOf(EcoGreen40, androidx.compose.ui.graphics.Color(0xFF245F3D)))
                    )
                    .clickable(onClick = onNewRoute)
                    .padding(22.dp),
            ) {
                Text(
                    text = "Nova comparação",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Compare rotas e veja\no impacto de CO2",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Button(
                    onClick = onNewRoute,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.White,
                        contentColor = EcoGreen40,
                    ),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(text = "Iniciar nova rota", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconArrowRight(tint = EcoGreen40, size = 16.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "CO2 evitado no total",
                    value = "${(insights.totalCo2AvoidedGrams / 1000).let { "%.1f".format(it) }} kg",
                    valueColor = EcoGreen40,
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Rotas comparadas",
                    value = "${insights.totalComparisons}",
                    valueColor = AppInk,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            insights.mostSustainableMode?.let { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppBlueTint)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(EcoBlue40, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        TransportModeIcon(mode = mode, tint = androidx.compose.ui.graphics.Color.White, size = 18.dp)
                    }
                    Text(
                        text = buildString {
                            append(mode.label)
                            append(" foi seu modal mais sustentável em ")
                            append(insights.mostSustainableModeCount)
                            append(" das últimas ")
                            append(insights.totalComparisons)
                            append(" comparações.")
                        },
                        color = androidx.compose.ui.graphics.Color(0xFF1D4650),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(text = label, color = AppInkMuted, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
