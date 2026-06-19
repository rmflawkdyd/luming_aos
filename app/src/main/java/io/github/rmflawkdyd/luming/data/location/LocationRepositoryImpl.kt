package io.github.rmflawkdyd.luming.data.location

import io.github.rmflawkdyd.luming.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dataSource: CoarseLocationDataSource,
) : LocationRepository {
    override fun hasPermission(): Boolean = dataSource.hasPermission()
    override suspend fun getCoarseLocation(): Pair<Double, Double>? =
        dataSource.getCoarseLocation()
}
