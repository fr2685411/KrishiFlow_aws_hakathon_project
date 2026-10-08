package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class FarmCoordinates(
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val isLiveGps: Boolean
)

class FarmLocationProvider(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getFarmCoordinates(): FarmCoordinates = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext getFallbackCoordinates("Nashik Farm Cluster (Default)")
        }

        try {
            val location = suspendCancellableCoroutine<Location?> { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // If last location is null, try current location with balanced power
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                                .addOnSuccessListener { currentLoc -> cont.resume(currentLoc) }
                                .addOnFailureListener { cont.resume(null) }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            }

            if (location != null) {
                val resolvedName = resolveLocationName(location.latitude, location.longitude)
                FarmCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationName = resolvedName,
                    isLiveGps = true
                )
            } else {
                getFallbackCoordinates("Nashik Hub (GPS Inactive)")
            }
        } catch (e: Exception) {
            Log.e("FarmLocationProvider", "Failed to retrieve device location: ${e.message}", e)
            getFallbackCoordinates("Nashik Hub (GPS Fallback)")
        }
    }

    private fun resolveLocationName(lat: Double, lon: Double): String {
        return try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLocality = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: "Farm Location"
                    val district = addr.adminArea ?: "India"
                    return "$subLocality, $district"
                }
            }
            "Farmgate GPS (${String.format(Locale.getDefault(), "%.2f", lat)}°N, ${String.format(Locale.getDefault(), "%.2f", lon)}°E)"
        } catch (e: Exception) {
            "Farmgate GPS (${String.format(Locale.getDefault(), "%.2f", lat)}°N, ${String.format(Locale.getDefault(), "%.2f", lon)}°E)"
        }
    }

    fun getFallbackCoordinates(reason: String = "Nashik Agricultural Hub"): FarmCoordinates {
        // High-density agricultural zone in Maharashtra (Dindori / Nashik)
        return FarmCoordinates(
            latitude = 20.0110,
            longitude = 73.7903,
            locationName = "Dindori Farm Hub, Nashik",
            isLiveGps = false
        )
    }
}
