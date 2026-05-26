package com.luming.domain.recommender

import com.luming.domain.model.Activity
import com.luming.domain.model.ContextSnapshot
import com.luming.domain.model.Recommendation

interface Recommender {
    fun recommend(library: List<Activity>, ctx: ContextSnapshot): List<Recommendation>
}
