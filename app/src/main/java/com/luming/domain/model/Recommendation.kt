package com.luming.domain.model

data class Recommendation(
    val activity: Activity,
    val rationale: String,
    val rank: Int,
)
