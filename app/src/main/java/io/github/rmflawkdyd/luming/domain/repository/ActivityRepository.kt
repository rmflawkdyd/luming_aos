package io.github.rmflawkdyd.luming.domain.repository

import io.github.rmflawkdyd.luming.domain.model.Activity

interface ActivityRepository {
    suspend fun getActivities(): List<Activity>
}
