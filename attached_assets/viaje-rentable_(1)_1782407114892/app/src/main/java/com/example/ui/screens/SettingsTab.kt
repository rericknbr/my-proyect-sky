package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(viewModel: TripViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Observe App Database Config
    val config by viewModel.config.collectAsState()

    // Toggles for simulating future features in the architecture block
    var overlayMock by remember { mutableStateOf(false) }
    var accUberMock by remember { mutableStateOf(false) }
    var accDidiMock by remember { mutableStateOf(false) }
    var autoOcrMock by remember { mutableStateOf(false) }

    // Floating bubble toggle in main screen
    val isBubbleVisible by viewModel.isFloatingWidgetVisible.collectAsState()

    // Limit Input Fields
    var limitGreenStr by remember { mutableStateOf("") }
    var limitYellowStr by remember { mutableStateOf("") }

    // Populate local states when config loads
    LaunchedEffect(config) {
        config?.let {
            if (limitGreenStr.isEmpty()) limitGreenStr = it.limitGreen.toString()
            if (limitYellowStr.isEmpty()) limitYellowStr = it.limitYellow.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title block
        Column {
            Text(
                text = "Configuración",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NeonYellowGreen
            )
            Text(
                text = "Personaliza límites e integra automatizaciones",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // SEMAPHORE CARD CONFIGURATION
        Text(
            text = "Parámetros de Semáforo:",
            fontSize = 14.sp,
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
                Text(
                    text = "Ajustar valores mínimos de Rentabilidad por KM:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                // Green limit minimum input
                OutlinedTextField(
                    value = limitGreenStr,
                    onValueChange = { limitGreenStr = it },
                    label = { Text("Límite Verde (🟢 Excelente / km)", color = TextSecondary) },
                    placeholder = { Text("Ej. 8.0", color = TextSecondary.copy(alpha = 0.5f)) },
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
                        .testTag("green_limit_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SemGreen)
                    }
                )

                // Yellow limit minimum input
                OutlinedTextField(
                    value = limitYellowStr,
                    onValueChange = { limitYellowStr = it },
                    label = { Text("Límite Amarillo (🟡 Aceptable / km)", color = TextSecondary) },
                    placeholder = { Text("Ej. 6.0", color = TextSecondary.copy(alpha = 0.5f)) },
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
                        .testTag("yellow_limit_input"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.TrendingFlat, contentDescription = null, tint = SemYellow)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val green = limitGreenStr.toDoubleOrNull() ?: 8.0
                            val yellow = limitYellowStr.toDoubleOrNull() ?: 6.0
                            if (green <= yellow) {
                                Toast.makeText(context, "El límite verde debe ser mayor que el amarillo", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.updateConfig(green, yellow)
                                Toast.makeText(context, "Configuración guardada localmente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonYellowGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("save_config_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Límites", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FLOATING WIDGET OPTION
        Text(
            text = "Simulación de Burbuja de Ayuda:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Burbuja de Asistencia en Pantalla",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Muestra un flotador interactivo en tiempo real con ganancias rápidas.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = isBubbleVisible,
                    onCheckedChange = { isChecked ->
                        viewModel.isFloatingWidgetVisible.value = isChecked
                        try {
                            val intent = android.content.Intent(context, com.example.service.FloatingWidgetService::class.java)
                            if (isChecked) {
                                if (android.provider.Settings.canDrawOverlays(context)) {
                                    context.startService(intent)
                                } else {
                                    Toast.makeText(context, "Por favor autorice 'Mostrar sobre otras apps' primero", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                context.stopService(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonYellowGreen,
                        checkedTrackColor = NeonYellowGreen.copy(alpha = 0.4f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = SecondaryDark
                    ),
                    modifier = Modifier.testTag("floating_widget_toggle")
                )
            }
        }

        // ROADMAP & FUTURE ARCHITECTURE PREPARED PANEL
        Text(
            text = "Permisos de Segundo Plano y Automatización:",
            fontSize = 14.sp,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Servicio de Accesibilidad Integrado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonYellowGreen
                    )
                    Text(
                        text = "Activa el servicio para capturar ganancias y distancias de las tarjetas de Uber Driver y DiDi de forma 100% automática en tiempo real.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Divider(color = SecondaryDark)

                // 1. Accessibility Activation Button
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CircleNotifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = NeonYellowGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Servicio de Captura en 2do Plano",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Para que la app detecte viajes en segundo plano y sobreponga el cálculo real al recibir ofertas.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                                Toast.makeText(context, "Busca 'Viaje Rentable' en servicios instalados y actívalo", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir ajustes de accesibilidad", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryDark,
                            contentColor = NeonYellowGreen
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Activar Servicio de Accesibilidad", fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = SecondaryDark)

                // 2. Draw Over Other Apps Overlay Activation Button
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = NeonYellowGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Autorizar Mostrar sobre Otras Apps",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Permite a la burbuja flotante del velocímetro de rentabilidad mostrarse encima de Uber Driver o DiDi Conductor.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                Toast.makeText(context, "Busca 'Viaje Rentable' y activa el permiso", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                // Fallback for settings page
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "No se pudo abrir la configuración de pantallas superpuestas", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryDark,
                            contentColor = NeonYellowGreen
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Autorizar Dibujar sobre Otras Apps", fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = SecondaryDark)

                // Additional structural roadmaps
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "🚀 Próximamente:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NeonYellowGreen
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Estadísticas diarias comparativas (Uber vs DiDi)", fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Historial avanzado exportable a Excel/CSV", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
