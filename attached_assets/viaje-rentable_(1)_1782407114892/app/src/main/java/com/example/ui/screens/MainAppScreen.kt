package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.TripRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.TripViewModel
import com.example.ui.viewmodel.TripViewModelFactory
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TripViewModel) {
    // Current active tab index (0 = Calculator, 1 = History, 2 = Settings)
    var selectedTab by remember { mutableStateOf(0) }

    // Floating bubble state
    val isBubbleVisible by viewModel.isFloatingWidgetVisible.collectAsState()
    val isBubbleExpanded by viewModel.isFloatingWidgetExpanded.collectAsState()

    // Drag offset for our floating bubble simulation
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Live calculations for bubble parameters
    val earningsInput by viewModel.earningsInput.collectAsState()
    val pickupInput by viewModel.pickupInput.collectAsState()
    val pickupUnit by viewModel.pickupUnit.collectAsState()
    val tripInput by viewModel.tripInput.collectAsState()
    val config by viewModel.config.collectAsState()

    val liveEarnings = earningsInput.toDoubleOrNull() ?: 0.0
    val livePickup = pickupInput.toDoubleOrNull() ?: 0.0
    val liveTrip = tripInput.toDoubleOrNull() ?: 0.0
    val livePickupKm = if (pickupUnit == "m") livePickup / 1000.0 else livePickup
    val liveTotalDistance = if (pickupInput.isEmpty() && tripInput.isEmpty()) 0.0 else livePickupKm + liveTrip
    val liveRealKm = if (liveTotalDistance > 0.0) liveEarnings / liveTotalDistance else 0.0

    val currentConfig = config ?: com.example.data.model.AppConfig()
    val liveStatus = when {
        liveRealKm >= currentConfig.limitGreen -> "EXCELENTE"
        liveRealKm >= currentConfig.limitYellow -> "ACEPTABLE"
        else -> "MALO"
    }

    val liveStatusColor = when (liveStatus) {
        "EXCELENTE" -> SemGreen
        "ACEPTABLE" -> SemYellow
        else -> SemRed
    }

    val liveStatusEmoji = when (liveStatus) {
        "EXCELENTE" -> "🟢"
        "ACEPTABLE" -> "🟡"
        else -> "🔴"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculadora") },
                    label = { Text("Calculadora") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonYellowGreen,
                        indicatorColor = NeonYellowGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_calculator")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonYellowGreen,
                        indicatorColor = NeonYellowGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_history")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Configurar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = NeonYellowGreen,
                        indicatorColor = NeonYellowGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching
            when (selectedTab) {
                0 -> CalculatorTab(viewModel)
                1 -> HistoryTab(viewModel)
                2 -> SettingsTab(viewModel)
            }

            // SIMULATED FLOATING BUBBLE OVERLAY
            if (isBubbleVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Empty click sink to prevent child taps from bleeding into active widgets underneath if expanded
                        }
                ) {
                    if (!isBubbleExpanded) {
                        // Compact Mode Bubble (small, draggable pill)
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 32.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.9f))
                                .clickable { viewModel.isFloatingWidgetExpanded.value = true }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                    }
                                }
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                        colors = listOf(
                                            SecondaryDark,
                                            liveStatusColor.copy(alpha = 0.3f),
                                            SecondaryDark
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("floating_bubble_compact"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = liveStatusEmoji,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (liveEarnings == 0.0) "$--.-/km" else String.format(Locale.getDefault(), "$%.2f/km", liveRealKm),
                                    color = liveStatusColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else {
                        // Expanded Mode Bubble (small modal overlay on bottom right)
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .shadow(12.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .width(280.dp)
                                .padding(14.dp)
                                .testTag("floating_bubble_expanded")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Mini Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AirportShuttle,
                                            contentDescription = null,
                                            tint = NeonYellowGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Burbuja Asistente",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonYellowGreen
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.isFloatingWidgetExpanded.value = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Minimize,
                                            contentDescription = "Minimizar",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Divider(color = SecondaryDark)

                                // Information details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Ganancia", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = String.format(Locale.getDefault(), "$%.2f", liveEarnings),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Trayecto Total", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", liveTotalDistance)} km",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Viaje Cliente", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", liveTrip)} km",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Recogida", fontSize = 10.sp, color = TextSecondary)
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", livePickup)} $pickupUnit",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                // High Contrast Speedometer Metrics
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SecondaryDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "EFICIENCIA REAL / KM",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = if (liveEarnings == 0.0) "$--.--/km" else String.format(Locale.getDefault(), "$%.2f/km", liveRealKm),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = liveStatusColor
                                    )
                                    Text(
                                        text = "$liveStatusEmoji $liveStatus",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = liveStatusColor
                                    )
                                }

                                // Interactive Info Footer
                                Text(
                                    text = "Arrastra la burbuja para colocarla donde quieras.",
                                    fontSize = 9.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
