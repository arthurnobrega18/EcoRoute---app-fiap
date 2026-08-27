package com.ecoroute.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ecoroute.domain.ModeEstimate
import com.ecoroute.ui.components.AppTopBar
import com.ecoroute.ui.icons.IconAlertTriangle
import com.ecoroute.ui.icons.IconLeaf
import com.ecoroute.ui.icons.IconRefresh
import com.ecoroute.ui.theme.AppBackground
import com.ecoroute.ui.theme.AppBorder
import com.ecoroute.ui.theme.AppInk
import com.ecoroute.ui.theme.AppInkMuted
import com.ecoroute.ui.theme.AppSurface
import com.ecoroute.ui.theme.EcoGreen40
import com.ecoroute.ui.theme.EmissionTier
import com.ecoroute.ui.theme.TransportModeIcon
import com.ecoroute.ui.theme.emissionTier
import com.ecoroute.ui.theme.label
import kotlin.math.roundToInt

@Composable
fun ComparisonScreen(
    origin: String,
    destination: String,
    onInsights: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ComparisonViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = createSavedStateHandle().apply {
                    set("origin", origin)
                    set("destination", destination)
                }
                ComparisonViewModel(application, savedStateHandle)
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = AppBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp),
        ) {
            AppTopBar(title = "Comparar rotas", onBack = onBack)
            Text(
                text = "$origin  →  $destination",
                color = AppInkMuted,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
            )

            when (val state = uiState) {
                is ComparisonUiState.Loading -> LoadingContent()
                is ComparisonUiState.Error -> ErrorContent(message = state.message, onRetry = viewModel::retry)
                is ComparisonUiState.Success -> SuccessContent(
                    estimates = state.estimates,
                    failedModesCount = state.failedModesCount,
                    onInsights = onInsights,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(width = 4.dp, color = AppBorder, shape = androidx.compose.foundation.shape.CircleShape),
        ) {
            CircularProgressIndicator(
                color = EcoGreen40,
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = "Calculando as rotas...",
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Consultando distância e emissão por modal",
            color = AppInkMuted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 30.dp),
        )
        repeat(3) {
            SkeletonRow()
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SkeletonRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEECE3)),
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Box(modifier = Modifier.size(width = 70.dp, height = 12.dp).background(Color(0xFFEEECE3), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(width = 100.dp, height = 10.dp).background(Color(0xFFEEECE3), RoundedCornerShape(4.dp)))
        }
        Box(modifier = Modifier.size(width = 44.dp, height = 16.dp).background(Color(0xFFEEECE3), RoundedCornerShape(4.dp)))
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFFFBE4DD)),
            contentAlignment = Alignment.Center,
        ) {
            IconAlertTriangle(tint = Color(0xFFB24628))
        }
        Text(
            text = "Não foi possível calcular esta rota",
            color = AppInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        )
        Text(
            text = message,
            color = AppInkMuted,
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen40),
        ) {
            IconRefresh(tint = Color.White, size = 16.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Tentar novamente", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
        }
    }
}

@Composable
private fun SuccessContent(
    estimates: List<ModeEstimate>,
    failedModesCount: Int,
    onInsights: () -> Unit,
) {
    val best = estimates.minByOrNull { it.emissionGrams }
    val worst = estimates.maxByOrNull { it.emissionGrams }
    val maxEmission = worst?.emissionGrams?.takeIf { it > 0 } ?: 1.0

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (failedModesCount > 0) {
                item {
                    Text(
                        text = "$failedModesCount modal(is) não puderam ser calculados.",
                        color = AppInkMuted,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            if (best != null && worst != null && worst.emissionGrams > best.emissionGrams) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmissionTier.ZERO.tint)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconLeaf(tint = EcoGreen40, size = 18.dp)
                        Text(
                            text = "Indo de ${best.mode.label.lowercase()} você evita " +
                                "${(worst.emissionGrams - best.emissionGrams).roundToInt()} g de CO2 em comparação ao ${worst.mode.label.lowercase()}.",
                            color = Color(0xFF1E5233),
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
            items(estimates) { estimate ->
                ModeEstimateCard(
                    estimate = estimate,
                    isBest = estimate === best && best !== worst,
                    isWorst = estimate === worst && best !== worst,
                    maxEmission = maxEmission,
                )
            }
        }

        Text(
            text = "Comparação salva no histórico.",
            color = AppInkMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 14.dp),
        )
        Button(
            onClick = onInsights,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen40),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 20.dp),
        ) {
            Text(text = "Ver meus insights", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ModeEstimateCard(
    estimate: ModeEstimate,
    isBest: Boolean,
    isWorst: Boolean,
    maxEmission: Double,
) {
    val tier = estimate.mode.emissionTier
    val borderColor = when {
        isBest -> EcoGreen40
        isWorst -> tier.color
        else -> AppBorder
    }
    val borderWidth = if (isBest || isWorst) 1.5.dp else 1.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppSurface)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        if (isBest) {
            Badge(text = "MAIS SUSTENTÁVEL", background = EcoGreen40)
            Spacer(modifier = Modifier.height(8.dp))
        } else if (isWorst) {
            Badge(text = "MAIOR IMPACTO", background = tier.color)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tier.tint),
                contentAlignment = Alignment.Center,
            ) {
                TransportModeIcon(mode = estimate.mode, tint = tier.color)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = estimate.mode.label, color = AppInk, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "${"%.1f".format(estimate.distanceKm)} km · ${estimate.durationMin.roundToInt()} min",
                    color = AppInkMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${estimate.emissionGrams.roundToInt()} g",
                    color = tier.color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                )
                Text(text = "CO2", color = AppInkMuted, fontSize = 10.5.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(tier.tint),
        ) {
            val fraction = (estimate.emissionGrams / maxEmission).coerceIn(0.06, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxSize()
                    .background(tier.color),
            )
        }
    }
}

@Composable
private fun Badge(text: String, background: Color) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    )
}
