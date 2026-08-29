package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrRecentlyAdded
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStream
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSummary
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TracearrUiState(
    val loading: Boolean = false,
    val loadingMoreHistory: Boolean = false,
    val loadingMoreRecentlyAdded: Boolean = false,
    val hasInstance: Boolean = false,
    val streams: List<TracearrStream> = emptyList(),
    val streamSummary: TracearrStreamSummary? = null,
    val history: List<TracearrHistoryItem> = emptyList(),
    val historyNextCursor: String? = null,
    val recentlyAdded: List<TracearrRecentlyAdded> = emptyList(),
    val recentlyAddedNextCursor: String? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TracearrViewModel(
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TracearrUiState())
    val state: StateFlow<TracearrUiState> = _state.asStateFlow()

    private val currentRepository = getTracearrInstanceRepositoryUseCase
        .observeSelected()
        .distinctUntilChanged { old, new -> old?.instance?.id == new?.instance?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        currentRepository
            .onEach { repo ->
                if (repo == null) {
                    _state.value = TracearrUiState()
                } else {
                    _state.value = _state.value.copy(hasInstance = true)
                    refresh()
                }
            }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.streams ?: flowOf(null) }
            .onEach { result ->
                if (result is NetworkResult.Success) _state.value = _state.value.copy(streams = result.data)
            }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.streamSummary ?: flowOf(null) }
            .onEach { summary -> _state.value = _state.value.copy(streamSummary = summary) }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.history ?: flowOf(null) }
            .onEach { result ->
                if (result is NetworkResult.Success) _state.value = _state.value.copy(history = result.data)
            }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.historyNextCursor ?: flowOf(null) }
            .onEach { cursor -> _state.value = _state.value.copy(historyNextCursor = cursor) }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.recentlyAdded ?: flowOf(null) }
            .onEach { result ->
                if (result is NetworkResult.Success) _state.value = _state.value.copy(recentlyAdded = result.data)
            }
            .launchIn(viewModelScope)

        currentRepository
            .flatMapLatest { it?.recentlyAddedNextCursor ?: flowOf(null) }
            .onEach { cursor -> _state.value = _state.value.copy(recentlyAddedNextCursor = cursor) }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, errorMessage = null)
            val results = listOf(repo.refreshStreams(), repo.refreshHistory(), repo.refreshRecentlyAdded())
            val firstError = results.filterIsInstance<NetworkResult.Error>().firstOrNull()
            _state.value = _state.value.copy(loading = false, errorMessage = firstError?.message)
        }
    }

    fun loadMoreHistory() {
        val repo = currentRepository.value ?: return
        if (_state.value.loadingMoreHistory || _state.value.historyNextCursor == null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingMoreHistory = true)
            val result = repo.loadMoreHistory()
            val error = (result as? NetworkResult.Error)?.message
            _state.value = _state.value.copy(loadingMoreHistory = false, errorMessage = error)
        }
    }

    fun loadMoreRecentlyAdded() {
        val repo = currentRepository.value ?: return
        if (_state.value.loadingMoreRecentlyAdded || _state.value.recentlyAddedNextCursor == null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingMoreRecentlyAdded = true)
            val result = repo.loadMoreRecentlyAdded()
            val error = (result as? NetworkResult.Error)?.message
            _state.value = _state.value.copy(loadingMoreRecentlyAdded = false, errorMessage = error)
        }
    }
}
