package com.example.data.weather

import android.content.Context
import android.util.Log
import com.example.data.location.FarmCoordinates
import com.example.data.location.FarmLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    private val locationProvider = FarmLocationProvider(context)
    private val apiService = WeatherApiService.create()

    private val _weatherState = MutableStateFlow<FarmWeatherDashboardData>(
        WeatherAdvisoryEngine.generateDashboardData(
            response = null,
            coords = locationProvider.getFallbackCoordinates()
        )
    )
    val weatherState: StateFlow<FarmWeatherDashboardData> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refreshWeather(requestGps = false)
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    fun refreshWeather(requestGps: Boolean = true) {
        externalScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                val coords = if (requestGps && locationProvider.hasLocationPermission()) {
                    locationProvider.getFarmCoordinates()
                } else {
                    locationProvider.getFallbackCoordinates()
                }

                val response = try {
                    apiService.getLiveForecast(
                        latitude = coords.latitude,
                        longitude = coords.longitude
                    )
                } catch (e: Exception) {
                    Log.w("WeatherRepository", "Open-Meteo live fetch failed, using advisory simulation: ${e.message}")
                    null
                }

                val dashboardData = WeatherAdvisoryEngine.generateDashboardData(response, coords)
                _weatherState.value = dashboardData
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Failed to update weather: ${e.message}", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
