package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TripHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTab(viewModel: TripViewModel) {
    val history by viewModel.history.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Vaciar Historial", color = Color.White) },
            text = { Text("¿Estás seguro de que deseas eliminar permanentemente todos los viajes guardados?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Historial vaciado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_history")
                ) {
                    Text("Borrar Todo", color = SemRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historial",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonYellowGreen
                )
                Text(
                    text = "Control de tus viajes y rentabilidad",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .background(SemRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Borrar todo",
                        tint = SemRed
                    )
                }
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "Empty History",
                        tint = TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin viajes guardados",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Haz tus cálculos en la calculadora y presiona 'Guardar en Historial' para registrar tu actividad de Uber/DiDi.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { trip ->
                    HistoryItemCard(trip = trip, onDelete = {
                        viewModel.deleteHistoryItem(trip)
                        Toast.makeText(context, "Viaje eliminado", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(trip: TripHistory, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateStr = remember(trip.timestamp) { sdf.format(Date(trip.timestamp)) }

    val platformColor = when (trip.platform) {
        "Uber" -> Color.White
        "DiDi" -> DidiOrange
        else -> TextSecondary
    }

    val stateColor = when (trip.status) {
        "EXCELENTE" -> SemGreen
        "ACEPTABLE" -> SemYellow
        else -> SemRed
    }

    val stateIcon = when (trip.status) {
        "EXCELENTE" -> "🟢"
        "ACEPTABLE" -> "🟡"
        else -> "🔴"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${trip.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(platformColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trip.platform,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ganancia: ", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", trip.earnings),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Row {
                        Text("Recogida: ", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${trip.pickupDistance} ${trip.pickupUnit}",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Viaje: ", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${trip.tripDistance} km",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row {
                        Text("Distancia Total: ", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", trip.totalDistance)} km",
                            fontSize = 11.sp,
                            color = NeonYellowGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Colored tag for real efficiency
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f/km", trip.earningsPerKmReal),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = stateColor
                    )
                    Text(
                        text = "$stateIcon ${trip.status}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }
            }
        }
    }
}
