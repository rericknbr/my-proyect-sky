package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.TripViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTab(viewModel: TripViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Observe state flows
    val earningsInput by viewModel.earningsInput.collectAsState()
    val pickupInput by viewModel.pickupInput.collectAsState()
    val pickupUnit by viewModel.pickupUnit.collectAsState()
    val tripInput by viewModel.tripInput.collectAsState()
    val platformInput by viewModel.platformInput.collectAsState()
    val config by viewModel.config.collectAsState()

    // Real-time calculation helpers
    val liveEarnings = earningsInput.toDoubleOrNull() ?: 0.0
    val livePickup = pickupInput.toDoubleOrNull() ?: 0.0
    val liveTrip = tripInput.toDoubleOrNull() ?: 0.0
    val livePickupKm = if (pickupUnit == "m") livePickup / 1000.0 else livePickup
    val liveTotalDistance = if (pickupInput.isEmpty() && tripInput.isEmpty()) 0.0 else livePickupKm + liveTrip
    val liveRealKm = if (liveTotalDistance > 0.0) liveEarnings / liveTotalDistance else 0.0
    val liveTripKm = if (liveTrip > 0.0) liveEarnings / liveTrip else 0.0

    // Traffic light live status
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

    // Has clicked calculation result
    val explicitResult by viewModel.calculationResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Branding Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = R.drawable.viaje_rentable_banner_1782166047408),
                contentDescription = "Viaje Rentable Header",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Viaje Rentable",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonYellowGreen
                )
                Text(
                    text = "Calculadora Inteligente de Kilometraje",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Form Inputs Block
        Text(
            text = "Calcular Nuevo Viaje:",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ganancia ($)
                OutlinedTextField(
                    value = earningsInput,
                    onValueChange = { viewModel.earningsInput.value = it },
                    label = { Text("Ganancia ($)", color = TextSecondary) },
                    placeholder = { Text("Ej. 119.51", color = TextSecondary.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = NeonYellowGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("earnings_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = NeonYellowGreen)
                    }
                )

                // Distancia de recogida & selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pickupInput,
                        onValueChange = { viewModel.pickupInput.value = it },
                        label = { Text("Distancia de recogida", color = TextSecondary) },
                        placeholder = { Text("Ej. 1.4", color = TextSecondary.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = NeonYellowGreen
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("pickup_input"),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary)
                        }
                    )

                    // Unit Selector (km / m)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryDark),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("km", "m").forEach { unit ->
                            val isUnitSelected = pickupUnit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(if (isUnitSelected) NeonYellowGreen else Color.Transparent)
                                    .clickable { viewModel.pickupUnit.value = unit }
                                    .testTag("unit_pill_$unit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnitSelected) Color.Black else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Distancia del viaje
                OutlinedTextField(
                    value = tripInput,
                    onValueChange = { viewModel.tripInput.value = it },
                    label = { Text("Distancia del viaje (km)", color = TextSecondary) },
                    placeholder = { Text("Ej. 20.5", color = TextSecondary.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = NeonYellowGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Map, contentDescription = null, tint = TextSecondary)
                    }
                )

                // Calculate Action Button
                Button(
                    onClick = {
                        viewModel.calculateTrip()
                        Toast.makeText(context, "Calculado y actualizado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("calculate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonYellowGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calcular", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // RESULTS PANEL (Tiempo real o gatillado)
        Text(
            text = "Resultados en Tiempo Real:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NeonYellowGreen
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header of result: Signal status representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semáforo de Rentabilidad",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Surface(
                        color = liveStatusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy() 
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$liveStatusEmoji $liveStatus",
                                color = liveStatusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Divider(color = SecondaryDark)

                // Metrics Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ganancia:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", liveEarnings),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recogida:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", livePickup)} $pickupUnit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Viaje:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f km", liveTrip),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Distancia Total:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f km", liveTotalDistance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellowGreen
                        )
                    }
                }

                Divider(color = SecondaryDark)

                // LARGE NUMBERS / Dashboards style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecondaryDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ganancia por KM Real",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f/km", liveRealKm),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = liveStatusColor
                    )
                    Text(
                        text = "(Incluye recogida + viaje)",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ganancia por KM de Viaje:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "(Solo trayecto del cliente)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f/km", liveTripKm),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // SAVE IN HISTORY ACTION
                Button(
                    onClick = {
                        viewModel.saveCurrentToHistory()
                        Toast.makeText(context, "Viaje guardado en el historial", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_to_history_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryDark,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar en Historial", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
