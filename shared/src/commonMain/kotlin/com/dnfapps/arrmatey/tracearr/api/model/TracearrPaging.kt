package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TracearrMeta(
    val nextCursor: String? = null
)

@Serializable
data class TracearrPagedResponse<T>(
    val data: List<T> = emptyList(),
    val meta: TracearrMeta = TracearrMeta()
)
