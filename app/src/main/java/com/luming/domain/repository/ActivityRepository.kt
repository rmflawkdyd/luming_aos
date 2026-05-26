package com.luming.domain.repository

import com.luming.domain.model.Activity

interface ActivityRepository {
    suspend fun getActivities(): List<Activity>
}
