package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrLibrary(
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_type") val serverType: String? = null,
    @SerialName("library_id") val libraryId: String? = null,
    @SerialName("item_count") val itemCount: Long = 0,
    @SerialName("movie_count") val movieCount: Long = 0,
    @SerialName("episode_count") val episodeCount: Long = 0,
    @SerialName("show_count") val showCount: Long = 0,
    @SerialName("track_count") val trackCount: Long = 0,
    @SerialName("total_file_size") val totalFileSize: Long = 0,
    val resolutions: Map<String, Int> = emptyMap()
)
