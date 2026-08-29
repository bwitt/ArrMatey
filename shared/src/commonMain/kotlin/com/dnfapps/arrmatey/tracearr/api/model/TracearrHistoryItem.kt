package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrHistoryUser(
    @SerialName("server_user_id") val serverUserId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    @SerialName("identity_name") val identityName: String? = null
)

@Serializable
data class TracearrHistoryItem(
    val id: String? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("server_type") val serverType: String? = null,
    @SerialName("media_title") val mediaTitle: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("show_title") val showTitle: String? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int? = null,
    val year: Int? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("stopped_at") val stoppedAt: String? = null,
    @SerialName("percent_complete") val percentComplete: Double? = null,
    val watched: Boolean? = null,
    @SerialName("segment_count") val segmentCount: Int? = null,
    val device: String? = null,
    val player: String? = null,
    val platform: String? = null,
    @SerialName("is_transcode") val isTranscode: Boolean? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null,
    val user: TracearrHistoryUser? = null
)
