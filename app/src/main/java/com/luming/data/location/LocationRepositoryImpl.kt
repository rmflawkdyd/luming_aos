package com.luming.data.location

import com.luming.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dataSource: CoarseLocationDataSource,
) : LocationRepository {
    override suspend fun getCoarseLocation(): Pair<Double, Double>? =
        dataSource.getCoarseLocation()
}
