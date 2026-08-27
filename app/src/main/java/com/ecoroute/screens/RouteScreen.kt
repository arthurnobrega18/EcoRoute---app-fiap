package com.ecoroute.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ecoroute.location.getLastKnownLocation
import com.ecoroute.ui.components.AppTopBar
import com.ecoroute.ui.icons.IconArrowRight
import com.ecoroute.ui.icons.IconFlag
import com.ecoroute.ui.icons.IconLocationPin
import com.ecoroute.ui.theme.AppBackground
import com.ecoroute.ui.theme.AppBorder
import com.ecoroute.ui.theme.AppGreenTint
import com.ecoroute.ui.theme.AppInk
import com.ecoroute.ui.theme.AppInkMuted
import com.ecoroute.ui.theme.AppSurface
import com.ecoroute.ui.theme.EcoGreen40
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val DEFAULT_MAP_CENTER = LatLng(-23.5505, -46.6333) // São Paulo, usado até a origem estar disponível

private fun parseLatLng(text: String): LatLng? {
    val parts = text.split(",").map { it.trim() }
    if (parts.size != 2) return null
    val lat = parts[0].toDoubleOrNull() ?: return null
    val lng = parts[1].toDoubleOrNull() ?: return null
    return LatLng(lat, lng)
}

@Composable
fun RouteScreen(
    onCompare: (origin: String, destination: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var originFromGps by remember { mutableStateOf(false) }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun fillOriginFromCurrentLocation() {
        coroutineScope.launch {
            val location = runCatching { getLastKnownLocation(context) }.getOrNull()
            if (location != null && origin.isBlank()) {
                origin = "${location.latitude},${location.longitude}"
                originFromGps = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fillOriginFromCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            fillOriginFromCurrentLocation()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(containerColor = AppBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp),
        ) {
            AppTopBar(title = "Nova rota", onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppSurface)
                    .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(18.dp)),
            ) {
                RouteFieldRow(
                    label = "ORIGEM",
                    value = origin,
                    onValueChange = { origin = it; originFromGps = false },
                    placeholder = "De onde você sai?",
                    iconTint = EcoGreen40,
                    iconBackground = AppGreenTint,
                    icon = { tint -> IconLocationPin(tint = tint, size = 14.dp) },
                    trailingBadge = "GPS".takeIf { originFromGps },
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .height(1.dp)
                        .padding(start = 48.dp)
                        .fillMaxWidth()
                        .background(AppBorder),
                )
                RouteFieldRow(
                    label = "DESTINO",
                    value = destination,
                    onValueChange = { destination = it },
                    placeholder = "Para onde você vai?",
                    iconTint = Color(0xFFC1502E),
                    iconBackground = Color(0xFFFDEDE6),
                    icon = { tint -> IconFlag(tint = tint, size = 14.dp) },
                    trailingBadge = null,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val originLatLng = remember(origin) { parseLatLng(origin) }
            val destinationLatLng = remember(destination) { parseLatLng(destination) }
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(originLatLng ?: DEFAULT_MAP_CENTER, 14f)
            }
            LaunchedEffect(originLatLng) {
                originLatLng?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 14f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(width = 1.dp, color = AppBorder, shape = RoundedCornerShape(18.dp)),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {
                    originLatLng?.let { Marker(state = MarkerState(position = it), title = "Origem") }
                    destinationLatLng?.let { Marker(state = MarkerState(position = it), title = "Destino") }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onCompare(origin, destination) },
                enabled = origin.isNotBlank() && destination.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreen40),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
            ) {
                Text(text = "Comparar rotas", fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                Spacer(modifier = Modifier.width(8.dp))
                IconArrowRight(tint = Color.White, size = 17.dp)
            }
        }
    }
}

@Composable
private fun RouteFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconTint: Color,
    iconBackground: Color,
    icon: @Composable (Color) -> Unit,
    trailingBadge: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            icon(iconTint)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = label, color = AppInkMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(text = placeholder, color = AppInkMuted, fontSize = 14.5.sp) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = AppInk,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (trailingBadge != null) {
            Text(
                text = trailingBadge,
                color = EcoGreen40,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppGreenTint)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
