package io.github.rmflawkdyd.luming.domain.recommender

import io.github.rmflawkdyd.luming.domain.model.Activity
import io.github.rmflawkdyd.luming.domain.model.ContextSnapshot
import io.github.rmflawkdyd.luming.domain.model.Recommendation

interface Recommender {
    fun recommend(library: List<Activity>, ctx: ContextSnapshot): List<Recommendation>
}
