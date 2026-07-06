package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.TripRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TripViewModel
import com.example.ui.viewmodel.TripViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TripViewModel
    private var detectedReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TripRepository(database.tripDao())
        val factory = TripViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TripViewModel::class.java]

        // Dynamic BroadcastReceiver to listen to real-time driver screen detections
        detectedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val platform = intent.getStringExtra("platform") ?: "Otro"
                val earnings = intent.getDoubleExtra("earnings", 0.0)
                val pickup = intent.getDoubleExtra("pickup", 0.0)
                val pickupUnit = intent.getStringExtra("pickupUnit") ?: "km"
                val trip = intent.getDoubleExtra("trip", 0.0)

                if (earnings > 0.0) viewModel.earningsInput.value = earnings.toString()
                if (pickup > 0.0) viewModel.pickupInput.value = pickup.toString()
                viewModel.pickupUnit.value = pickupUnit
                if (trip > 0.0) viewModel.tripInput.value = trip.toString()
                viewModel.platformInput.value = platform

                // Recalculate automatic results
                viewModel.calculateTrip()

                Toast.makeText(
                    context,
                    "¡Viaje Rentable detectó datos de $platform automáticamente!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val filter = IntentFilter("com.example.VIAJE_RENTABLE_DETECTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(detectedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(detectedReceiver, filter)
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detectedReceiver?.let {
            unregisterReceiver(it)
        }
    }
}
