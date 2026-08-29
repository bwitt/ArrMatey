package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrRecentlyAdded(
    val id: String? = null,
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_type") val serverType: String? = null,
    @SerialName("library_id") val libraryId: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    val title: String? = null,
    val year: Int? = null,
    @SerialName("added_at") val addedAt: String? = null,
    @SerialName("removed_at") val removedAt: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null,
    @SerialName("tvdb_id") val tvdbId: Long? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("parent_rating_key") val parentRatingKey: String? = null,
    @SerialName("grandparent_rating_key") val grandparentRatingKey: String? = null
)
