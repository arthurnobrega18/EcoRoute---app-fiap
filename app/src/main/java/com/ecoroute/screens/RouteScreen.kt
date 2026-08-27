package com.ecoroute.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ecoroute.BuildConfig
import com.ecoroute.data.directions.DirectionsClient
import com.ecoroute.data.directions.decodePolyline
import com.ecoroute.location.PlaceAutocomplete
import com.ecoroute.location.PlaceSuggestion
import com.ecoroute.location.getCurrentLocation
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Usado como câmera inicial enquanto não há permissão de localização nem local selecionado.
private val BRAZIL_CENTER = LatLng(-14.235, -51.9253)
private const val BRAZIL_ZOOM = 4f
private const val FOCUS_ZOOM = 14f
private const val AUTOCOMPLETE_DEBOUNCE_MS = 1000L
private const val ROUTE_PREVIEW_MODE = "driving"

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
    val placeAutocomplete = remember { PlaceAutocomplete(context) }

    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var originFromGps by remember { mutableStateOf(false) }

    var originLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Texto já resolvido para uma coordenada (GPS ou place selecionado); evita refazer a busca
    // de predictions quando o texto do campo é atualizado programaticamente após uma seleção.
    var originResolvedText by remember { mutableStateOf<String?>(null) }
    var destinationResolvedText by remember { mutableStateOf<String?>(null) }

    var originSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }

    var originSessionToken by remember { mutableStateOf(placeAutocomplete.newSessionToken()) }
    var destinationSessionToken by remember { mutableStateOf(placeAutocomplete.newSessionToken()) }

    val effectiveOriginLatLng = originLatLng ?: remember(origin) { parseLatLng(origin) }
    val effectiveDestinationLatLng = destinationLatLng ?: remember(destination) { parseLatLng(destination) }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun fillOriginFromCurrentLocation() {
        coroutineScope.launch {
            val location = runCatching { getCurrentLocation(context) }.getOrNull()
            if (location != null && origin.isBlank()) {
                val text = "${location.latitude},${location.longitude}"
                origin = text
                originResolvedText = text
                originLatLng = LatLng(location.latitude, location.longitude)
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

    LaunchedEffect(origin) {
        if (origin.isBlank() || origin == originResolvedText) {
            originSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(AUTOCOMPLETE_DEBOUNCE_MS)
        originSuggestions = runCatching {
            placeAutocomplete.predictions(origin, originSessionToken)
        }.getOrDefault(emptyList())
    }

    LaunchedEffect(destination) {
        if (destination.isBlank() || destination == destinationResolvedText) {
            destinationSuggestions = emptyList()
            return@LaunchedEffect
        }
        delay(AUTOCOMPLETE_DEBOUNCE_MS)
        destinationSuggestions = runCatching {
            placeAutocomplete.predictions(destination, destinationSessionToken)
        }.getOrDefault(emptyList())
    }

    fun selectOriginSuggestion(suggestion: PlaceSuggestion) {
        origin = suggestion.primaryText
        originResolvedText = suggestion.primaryText
        originFromGps = false
        originSuggestions = emptyList()
        coroutineScope.launch {
            originLatLng = runCatching {
                placeAutocomplete.fetchLatLng(suggestion.placeId, originSessionToken)
            }.getOrNull()
            originSessionToken = placeAutocomplete.newSessionToken()
        }
    }

    fun selectDestinationSuggestion(suggestion: PlaceSuggestion) {
        destination = suggestion.primaryText
        destinationResolvedText = suggestion.primaryText
        destinationSuggestions = emptyList()
        coroutineScope.launch {
            destinationLatLng = runCatching {
                placeAutocomplete.fetchLatLng(suggestion.placeId, destinationSessionToken)
            }.getOrNull()
            destinationSessionToken = placeAutocomplete.newSessionToken()
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
                    onValueChange = {
                        origin = it
                        originFromGps = false
                        originLatLng = null
                    },
                    placeholder = "De onde você sai?",
                    iconTint = EcoGreen40,
                    iconBackground = AppGreenTint,
                    icon = { tint -> IconLocationPin(tint = tint, size = 14.dp) },
                    trailingBadge = "GPS".takeIf { originFromGps },
                    suggestions = originSuggestions,
                    onSuggestionSelected = ::selectOriginSuggestion,
                )
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .padding(start = 48.dp)
                        .fillMaxWidth()
                        .background(AppBorder),
                )
                RouteFieldRow(
                    label = "DESTINO",
                    value = destination,
                    onValueChange = {
                        destination = it
                        destinationLatLng = null
                    },
                    placeholder = "Para onde você vai?",
                    iconTint = Color(0xFFC1502E),
                    iconBackground = Color(0xFFFDEDE6),
                    icon = { tint -> IconFlag(tint = tint, size = 14.dp) },
                    trailingBadge = null,
                    suggestions = destinationSuggestions,
                    onSuggestionSelected = ::selectDestinationSuggestion,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

            LaunchedEffect(effectiveOriginLatLng, effectiveDestinationLatLng) {
                val originPoint = effectiveOriginLatLng
                val destinationPoint = effectiveDestinationLatLng
                if (originPoint == null || destinationPoint == null) {
                    routePoints = emptyList()
                    return@LaunchedEffect
                }
                routePoints = runCatching {
                    val response = DirectionsClient.service.getDirections(
                        origin = "${originPoint.latitude},${originPoint.longitude}",
                        destination = "${destinationPoint.latitude},${destinationPoint.longitude}",
                        mode = ROUTE_PREVIEW_MODE,
                        apiKey = BuildConfig.MAPS_API_KEY,
                    )
                    val encoded = response.routes.firstOrNull()?.overviewPolyline?.points
                    encoded?.let { decodePolyline(it) } ?: emptyList()
                }.getOrDefault(emptyList())
            }

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    effectiveOriginLatLng ?: BRAZIL_CENTER,
                    if (effectiveOriginLatLng != null) FOCUS_ZOOM else BRAZIL_ZOOM,
                )
            }
            LaunchedEffect(effectiveOriginLatLng, effectiveDestinationLatLng) {
                val originPoint = effectiveOriginLatLng
                val destinationPoint = effectiveDestinationLatLng
                when {
                    originPoint != null && destinationPoint == null ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(originPoint, FOCUS_ZOOM)
                    originPoint == null && destinationPoint != null ->
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(destinationPoint, FOCUS_ZOOM)
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
                    if (routePoints.isNotEmpty()) {
                        Polyline(points = routePoints, color = EcoGreen40, width = 8f)
                    }
                    effectiveOriginLatLng?.let { Marker(state = MarkerState(position = it), title = "Origem") }
                    effectiveDestinationLatLng?.let { Marker(state = MarkerState(position = it), title = "Destino") }

                    // Com os dois pontos definidos, enquadra origem + destino inteiros na câmera
                    // (precisa do GoogleMap já carregado para calcular bounds, daí o MapEffect,
                    // que só pode ser chamado dentro do content lambda do GoogleMap).
                    MapEffect(effectiveOriginLatLng, effectiveDestinationLatLng) { map ->
                        val originPoint = effectiveOriginLatLng
                        val destinationPoint = effectiveDestinationLatLng
                        if (originPoint != null && destinationPoint != null) {
                            val bounds = LatLngBounds.builder()
                                .include(originPoint)
                                .include(destinationPoint)
                                .build()
                            runCatching {
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                            }
                        }
                    }
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
    suggestions: List<PlaceSuggestion>,
    onSuggestionSelected: (PlaceSuggestion) -> Unit,
) {
    Column {
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

        if (suggestions.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                suggestions.forEach { suggestion ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionSelected(suggestion) }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = suggestion.primaryText,
                            color = AppInk,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (suggestion.secondaryText.isNotBlank()) {
                            Text(
                                text = suggestion.secondaryText,
                                color = AppInkMuted,
                                fontSize = 11.5.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
