package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.client.TracearrClient
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrLibrary
import com.dnfapps.arrmatey.tracearr.api.model.TracearrRecentlyAdded
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStream
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSummary
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TracearrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
) : InstanceScopedRepository {

    private val tracearrClient = TracearrClient(instance, httpClient)

    private val _streams = MutableStateFlow<NetworkResult<List<TracearrStream>>?>(null)
    val streams: StateFlow<NetworkResult<List<TracearrStream>>?> = _streams.asStateFlow()

    private val _streamSummary = MutableStateFlow<TracearrStreamSummary?>(null)
    val streamSummary: StateFlow<TracearrStreamSummary?> = _streamSummary.asStateFlow()

    private val _history = MutableStateFlow<NetworkResult<List<TracearrHistoryItem>>?>(null)
    val history: StateFlow<NetworkResult<List<TracearrHistoryItem>>?> = _history.asStateFlow()

    private val _historyNextCursor = MutableStateFlow<String?>(null)
    val historyNextCursor: StateFlow<String?> = _historyNextCursor.asStateFlow()

    private val _recentlyAdded = MutableStateFlow<NetworkResult<List<TracearrRecentlyAdded>>?>(null)
    val recentlyAdded: StateFlow<NetworkResult<List<TracearrRecentlyAdded>>?> = _recentlyAdded.asStateFlow()

    private val _recentlyAddedNextCursor = MutableStateFlow<String?>(null)
    val recentlyAddedNextCursor: StateFlow<String?> = _recentlyAddedNextCursor.asStateFlow()

    private val _libraries = MutableStateFlow<List<TracearrLibrary>>(emptyList())
    val libraries: StateFlow<List<TracearrLibrary>> = _libraries.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        tracearrClient.testConnection()

    suspend fun refreshStreams(serverId: String? = null): NetworkResult<List<TracearrStream>> {
        val result = tracearrClient.getStreams(serverId)
        result.onSuccess { envelope ->
            _streams.value = NetworkResult.Success(envelope.data)
            _streamSummary.value = envelope.summary
        }
        return result.map { it.data }
    }

    // Resets the history feed to the first page.
    suspend fun refreshHistory(serverId: String? = null): NetworkResult<List<TracearrHistoryItem>> {
        val result = tracearrClient.getHistory(cursor = null, serverId = serverId)
        result.onSuccess { page ->
            _history.value = NetworkResult.Success(page.data)
            _historyNextCursor.value = page.meta.nextCursor
        }
        return result.map { it.data }
    }

    // Appends the next history page; caller passes the cursor from historyNextCursor.
    suspend fun loadMoreHistory(serverId: String? = null): NetworkResult<List<TracearrHistoryItem>> {
        val cursor = _historyNextCursor.value ?: return NetworkResult.Success(emptyList())
        val result = tracearrClient.getHistory(cursor = cursor, serverId = serverId)
        result.onSuccess { page ->
            val current = (_history.value as? NetworkResult.Success)?.data.orEmpty()
            _history.value = NetworkResult.Success(current + page.data)
            _historyNextCursor.value = page.meta.nextCursor
        }
        return result.map { it.data }
    }

    suspend fun refreshRecentlyAdded(serverId: String? = null): NetworkResult<List<TracearrRecentlyAdded>> {
        val result = tracearrClient.getRecentlyAdded(cursor = null, serverId = serverId)
        result.onSuccess { page ->
            _recentlyAdded.value = NetworkResult.Success(page.data)
            _recentlyAddedNextCursor.value = page.meta.nextCursor
        }
        return result.map { it.data }
    }

    suspend fun loadMoreRecentlyAdded(serverId: String? = null): NetworkResult<List<TracearrRecentlyAdded>> {
        val cursor = _recentlyAddedNextCursor.value ?: return NetworkResult.Success(emptyList())
        val result = tracearrClient.getRecentlyAdded(cursor = cursor, serverId = serverId)
        result.onSuccess { page ->
            val current = (_recentlyAdded.value as? NetworkResult.Success)?.data.orEmpty()
            _recentlyAdded.value = NetworkResult.Success(current + page.data)
            _recentlyAddedNextCursor.value = page.meta.nextCursor
        }
        return result.map { it.data }
    }

    suspend fun refreshLibraries(): NetworkResult<List<TracearrLibrary>> =
        tracearrClient.getLibraries().onSuccess { _libraries.value = it }
}
