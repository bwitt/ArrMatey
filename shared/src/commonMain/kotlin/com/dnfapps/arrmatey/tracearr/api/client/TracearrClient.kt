package com.dnfapps.arrmatey.tracearr.api.client

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrLibrary
import com.dnfapps.arrmatey.tracearr.api.model.TracearrPagedResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrRecentlyAdded
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import io.ktor.client.request.url

class TracearrClient(
    val instance: Instance,
    private val httpClient: HttpClient
) {

    private val baseUrl: String
        get() {
            val cleanUrl = instance.getEffectiveBaseUrl().trim().trimEnd('|', '/', ' ')
            val apiBase = instance.type.apiBase.trim().trimStart('/', ' ')
            return "$cleanUrl/$apiBase"
        }

    suspend fun testConnection(): NetworkResult<Unit> =
        httpClient.safeGet("$baseUrl/${instance.type.testEndpoint}")

    suspend fun getStreams(serverId: String? = null): NetworkResult<TracearrStreamsResponse> =
        httpClient.safeGet("$baseUrl/streams") {
            if (!serverId.isNullOrBlank()) {
                url { parameters.append("server_id", serverId) }
            }
        }

    suspend fun getHistory(
        cursor: String? = null,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        serverId: String? = null
    ): NetworkResult<TracearrPagedResponse<TracearrHistoryItem>> =
        httpClient.safeGet("$baseUrl/history") {
            url {
                parameters.append("pageSize", pageSize.toString())
                if (!cursor.isNullOrBlank()) parameters.append("cursor", cursor)
                if (!serverId.isNullOrBlank()) parameters.append("server_id", serverId)
            }
        }

    suspend fun getRecentlyAdded(
        cursor: String? = null,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        serverId: String? = null
    ): NetworkResult<TracearrPagedResponse<TracearrRecentlyAdded>> =
        httpClient.safeGet("$baseUrl/recently-added") {
            url {
                parameters.append("pageSize", pageSize.toString())
                if (!cursor.isNullOrBlank()) parameters.append("cursor", cursor)
                if (!serverId.isNullOrBlank()) parameters.append("server_id", serverId)
            }
        }

    suspend fun getLibraries(): NetworkResult<List<TracearrLibrary>> =
        httpClient.safeGet("$baseUrl/libraries")

    companion object {
        private const val DEFAULT_PAGE_SIZE = 25
    }
}
