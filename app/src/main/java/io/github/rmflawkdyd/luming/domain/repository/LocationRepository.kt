package io.github.rmflawkdyd.luming.domain.repository

interface LocationRepository {
    fun hasPermission(): Boolean
    suspend fun getCoarseLocation(): Pair<Double, Double>?
}
