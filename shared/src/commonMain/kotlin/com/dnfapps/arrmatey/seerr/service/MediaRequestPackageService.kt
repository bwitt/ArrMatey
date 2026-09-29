package com.dnfapps.arrmatey.seerr.service

import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MediaRequestPackageService(
    private val client: SeerrClient,
    private val logger: Logger,
) {
    suspend fun enrichMedia(request: MediaRequest): MediaRequestPackage = coroutineScope {
        val detailsDeferred =
            async {
                when (request.type) {
                    RequestType.Movie -> fetchMovieDetails(request.media.tmdbId)
                    RequestType.Tv -> fetchTvDetails(request.media.tmdbId)
                    RequestType.Person -> null
                }
            }

        val serverDetailsDeferred =
            async {
                val serverId = request.serverId
                if (serverId != null && serverId > 0) {
                    when (request.type) {
                        RequestType.Movie -> fetchRadarrDetails(serverId)
                        RequestType.Tv -> fetchSonarrDetails(serverId)
                        RequestType.Person -> null
                    }
                } else {
                    null
                }
            }

        MediaRequestPackage(request, detailsDeferred.await(), serverDetailsDeferred.await())
    }

    suspend fun enrichRequests(requests: List<MediaRequest>): List<MediaRequestPackage> = coroutineScope {
        requests
            .map { request ->
                async { enrichMedia(request) }
            }.awaitAll()
    }

    private suspend fun fetchMovieDetails(tmdbId: Long): RequestMediaDetails? {
        var details: RequestMediaDetails? = null

        client
            .getMovieDetails(tmdbId)
            .onSuccess { movieDetails ->
                details = movieDetails
            }.onError { code, message, cause ->
                logger.error(cause) { "Error fetching movie details for $tmdbId: $message (code=$code)" }
            }

        return details
    }

    private suspend fun fetchTvDetails(tmdbId: Long): RequestMediaDetails? {
        var details: RequestMediaDetails? = null

        client
            .getTvDetails(tmdbId)
            .onSuccess { tvDetails ->
                details = tvDetails
            }.onError { code, message, cause ->
                logger.error(cause) { "Error fetching tv details for $tmdbId: $message (code=$code)" }
            }

        return details
    }

    private suspend fun fetchRadarrDetails(serverId: Long): ServiceDetails? {
        var details: ServiceDetails? = null

        client
            .getRadarrDetails(serverId)
            .onSuccess { radarrDetails ->
                details = radarrDetails
            }.onError { code, message, cause ->
                logger.error(cause) { "Error fetching radarr details for server $serverId: $message (code=$code)" }
            }

        return details
    }

    private suspend fun fetchSonarrDetails(serverId: Long): ServiceDetails? {
        var details: ServiceDetails? = null

        client
            .getSonarrDetails(serverId)
            .onSuccess { sonarrDetails ->
                details = sonarrDetails
            }.onError { code, message, cause ->
                logger.error(cause) { "Error fetching sonarr details for server $serverId: $message (code=$code)" }
            }

        return details
    }
}
