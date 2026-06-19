package io.github.rmflawkdyd.luming.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoarseLocationDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        private const val LOCATION_MAX_AGE_MS = 60 * 60 * 1000L // 1시간
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // hasPermission() 으로 호출 전 권한을 보장함
    suspend fun getCoarseLocation(): Pair<Double, Double>? {
        if (!hasPermission()) return null
        return try {
            val last = fusedClient.lastLocation.await()
            val raw = if (last != null && System.currentTimeMillis() - last.time < LOCATION_MAX_AGE_MS) {
                last
            } else {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
                ).await()
            } ?: return null
            val lat = (raw.latitude * 10).roundToInt() / 10.0
            val lon = (raw.longitude * 10).roundToInt() / 10.0
            Pair(lat, lon)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
