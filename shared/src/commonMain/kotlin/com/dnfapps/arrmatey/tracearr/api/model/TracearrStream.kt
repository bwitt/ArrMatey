package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrStreamsResponse(
    val data: List<TracearrStream> = emptyList(),
    val summary: TracearrStreamSummary? = null
)

@Serializable
data class TracearrStreamSummary(
    val total: Int = 0,
    val transcodes: Int = 0,
    @SerialName("direct_plays") val directPlays: Int = 0,
    @SerialName("total_bitrate") val totalBitrate: Long? = null
)

@Serializable
data class TracearrStream(
    val id: String? = null,
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("server_type") val serverType: String? = null,
    val username: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    @SerialName("user_avatar_url") val userAvatarUrl: String? = null,
    @SerialName("media_title") val mediaTitle: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("show_title") val showTitle: String? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int? = null,
    val year: Int? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("album_name") val albumName: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_path") val thumbPath: String? = null,
    @SerialName("duration_ms") val durationMs: Long? = null,
    @SerialName("progress_ms") val progressMs: Long? = null,
    val state: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("is_transcode") val isTranscode: Boolean? = null,
    val bitrate: Long? = null,
    val device: String? = null,
    val player: String? = null,
    val product: String? = null,
    val platform: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null
)
