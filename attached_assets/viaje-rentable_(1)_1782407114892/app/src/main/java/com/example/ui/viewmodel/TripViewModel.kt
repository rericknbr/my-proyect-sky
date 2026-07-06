package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppConfig
import com.example.data.model.TripHistory
import com.example.data.repository.TripRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    // Input States
    val earningsInput = MutableStateFlow("")
    val pickupInput = MutableStateFlow("")
    val pickupUnit = MutableStateFlow("km") // "km" or "m"
    val tripInput = MutableStateFlow("")
    val platformInput = MutableStateFlow("Uber") // "Uber", "DiDi", "Otro"

    // Configuration
    val config = repository.config.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppConfig()
    )

    // History
    val history = repository.allTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Advanced architecture placeholders
    val overlayEnabled = MutableStateFlow(false)
    val accessibilityUberEnabled = MutableStateFlow(false)
    val accessibilityDidiEnabled = MutableStateFlow(false)

    // Real-time calculations flow
    val earnings = earningsInput.map { it.toDoubleOrNull() ?: 0.0 }
    val pickup = pickupInput.map { it.toDoubleOrNull() ?: 0.0 }
    val trip = tripInput.map { it.toDoubleOrNull() ?: 0.0 }

    val totalDistance = combine(pickup, pickupInput, pickupUnit, trip) { p, pStr, unit, t ->
        if (pStr.isEmpty() && t == 0.0) return@combine 0.0
        val pKm = if (unit == "m") p / 1000.0 else p
        pKm + t
    }

    val earningsPerKmReal = combine(earnings, totalDistance) { e, tDist ->
        if (tDist > 0.0) e / tDist else 0.0
    }

    val earningsPerKmTrip = combine(earnings, trip) { e, tDist ->
        if (tDist > 0.0) e / tDist else 0.0
    }

    val status = combine(earningsPerKmReal, config) { realE, conf ->
        val currentConf = conf ?: AppConfig()
        when {
            realE >= currentConf.limitGreen -> "EXCELENTE"
            realE >= currentConf.limitYellow -> "ACEPTABLE"
            else -> "MALO"
        }
    }

    // Explicit calculation trigger model
    private val _calculationResult = MutableStateFlow<CalculationResult?>(null)
    val calculationResult = _calculationResult.asStateFlow()

    // Floating Widget State
    val isFloatingWidgetExpanded = MutableStateFlow(false)
    val isFloatingWidgetVisible = MutableStateFlow(true)

    init {
        // Ensure default config exists
        viewModelScope.launch {
            val existing = repository.getConfig()
            // Just to pre-populate / trigger fallback
            if (existing.limitGreen == 8.0 && existing.limitYellow == 6.0) {
                repository.saveConfig(existing)
            }
        }
    }

    // Load precompiled driver trip example
    fun loadExample(platform: String) {
        platformInput.value = platform
        if (platform == "Uber") {
            earningsInput.value = "119.51"
            pickupInput.value = "1.4"
            pickupUnit.value = "km"
            tripInput.value = "20.5"
        } else {
            earningsInput.value = "75.48"
            pickupInput.value = "524"
            pickupUnit.value = "m"
            tripInput.value = "4.1"
        }
        calculateTrip()
    }

    fun calculateTrip() {
        val e = earningsInput.value.toDoubleOrNull() ?: 0.0
        val p = pickupInput.value.toDoubleOrNull() ?: 0.0
        val unit = pickupUnit.value
        val t = tripInput.value.toDoubleOrNull() ?: 0.0

        val pKm = if (unit == "m") p / 1000.0 else p
        val totalDist = pKm + t
        val realKm = if (totalDist > 0.0) e / totalDist else 0.0
        val tripKm = if (t > 0.0) e / t else 0.0

        val conf = config.value ?: AppConfig()
        val stat = when {
            realKm >= conf.limitGreen -> "EXCELENTE"
            realKm >= conf.limitYellow -> "ACEPTABLE"
            else -> "MALO"
        }

        _calculationResult.value = CalculationResult(
            platform = platformInput.value,
            earnings = e,
            pickup = p,
            pickupUnit = unit,
            trip = t,
            totalDistance = totalDist,
            earningsPerKmReal = realKm,
            earningsPerKmTrip = tripKm,
            status = stat
        )
    }

    fun saveCurrentToHistory() {
        // fallback to live values if explicit calc not pressed yet
        val result = _calculationResult.value ?: run {
            calculateTrip()
            _calculationResult.value
        } ?: return

        viewModelScope.launch {
            repository.insertTrip(
                TripHistory(
                    platform = result.platform,
                    earnings = result.earnings,
                    pickupDistance = result.pickup,
                    pickupUnit = result.pickupUnit,
                    tripDistance = result.trip,
                    totalDistance = result.totalDistance,
                    earningsPerKmReal = result.earningsPerKmReal,
                    earningsPerKmTrip = result.earningsPerKmTrip,
                    status = result.status
                )
            )
        }
    }

    fun deleteHistoryItem(trip: TripHistory) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun updateConfig(green: Double, yellow: Double) {
        viewModelScope.launch {
            val newConf = AppConfig(limitGreen = green, limitYellow = yellow)
            repository.saveConfig(newConf)
        }
    }
}

data class CalculationResult(
    val platform: String,
    val earnings: Double,
    val pickup: Double,
    val pickupUnit: String,
    val trip: Double,
    val totalDistance: Double,
    val earningsPerKmReal: Double,
    val earningsPerKmTrip: Double,
    val status: String
)

class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
