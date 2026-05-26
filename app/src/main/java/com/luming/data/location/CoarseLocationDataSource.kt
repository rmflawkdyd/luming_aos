package com.luming.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoarseLocationDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCoarseLocation(): Pair<Double, Double>? {
        if (!hasPermission()) return null
        return try {
            val raw = fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
            ).await() ?: return null
            val lat = (raw.latitude * 10).roundToInt() / 10.0
            val lon = (raw.longitude * 10).roundToInt() / 10.0
            Pair(lat, lon)
        } catch (e: Exception) {
            null
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}
