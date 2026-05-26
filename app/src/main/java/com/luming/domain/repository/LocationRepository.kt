package com.luming.domain.repository

interface LocationRepository {
    suspend fun getCoarseLocation(): Pair<Double, Double>?
}
