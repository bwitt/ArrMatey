package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrRecentlyAdded
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStream
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrUiState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrViewModel
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.StringResource
import kotlin.math.roundToInt
import org.koin.compose.viewmodel.koinViewModel

private enum class TracearrTabSection(val labelRes: StringResource) {
    Streams(MR.strings.tracearr_streams),
    History(MR.strings.tracearr_history),
    RecentlyAdded(MR.strings.tracearr_recently_added)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracearrScreen(
    wideRailIsVisible: Boolean,
    viewModel: TracearrViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.tracearr)) },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        if (!state.hasInstance) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                NoInstanceView(type = InstanceType.Tracearr)
            }
            return@Scaffold
        }

        TracearrContent(
            state = state,
            onRefresh = viewModel::refresh,
            onLoadMoreHistory = viewModel::loadMoreHistory,
            onLoadMoreRecentlyAdded = viewModel::loadMoreRecentlyAdded,
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TracearrContent(
    state: TracearrUiState,
    onRefresh: () -> Unit,
    onLoadMoreHistory: () -> Unit,
    onLoadMoreRecentlyAdded: () -> Unit,
    paddingValues: PaddingValues
) {
    var selectedSection by remember { mutableStateOf(TracearrTabSection.Streams) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        SecondaryScrollableTabRow(selectedTabIndex = selectedSection.ordinal) {
            TracearrTabSection.entries.forEach { section ->
                Tab(
                    selected = selectedSection == section,
                    onClick = { selectedSection = section },
                    text = { Text(mokoString(section.labelRes)) }
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedSection) {
                TracearrTabSection.Streams -> StreamsList(state.streams)
                TracearrTabSection.History -> HistoryList(
                    items = state.history,
                    hasMore = state.historyNextCursor != null,
                    loadingMore = state.loadingMoreHistory,
                    onLoadMore = onLoadMoreHistory
                )
                TracearrTabSection.RecentlyAdded -> RecentlyAddedList(
                    items = state.recentlyAdded,
                    hasMore = state.recentlyAddedNextCursor != null,
                    loadingMore = state.loadingMoreRecentlyAdded,
                    onLoadMore = onLoadMoreRecentlyAdded
                )
            }
        }
    }
}

@Composable
private fun StreamsList(streams: List<TracearrStream>) {
    if (streams.isEmpty()) {
        EmptySection(mokoString(MR.strings.tracearr_no_streams))
        return
    }
    val unknown = mokoString(MR.strings.tracearr_label_unknown_title)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(streams, key = { it.id ?: it.hashCode().toString() }) { stream ->
            StreamCard(stream, unknown)
        }
    }
}

@Composable
private fun StreamCard(stream: TracearrStream, unknownTitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stream.mediaTitle ?: unknownTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            stream.username?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_user, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            stream.state?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_state, it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            stream.player?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_player, it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val duration = stream.durationMs ?: 0L
            val progress = stream.progressMs ?: 0L
            if (duration > 0) {
                val percent = ((progress.toDouble() / duration) * 100).roundToInt().coerceIn(0, 100)
                Text(
                    mokoString(MR.strings.tracearr_label_progress, percent.toString()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    items: List<TracearrHistoryItem>,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    if (items.isEmpty()) {
        EmptySection(mokoString(MR.strings.tracearr_no_history))
        return
    }
    val unknown = mokoString(MR.strings.tracearr_label_unknown_title)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id ?: it.hashCode().toString() }) { item ->
            HistoryCard(item, unknown)
        }
        if (hasMore) {
            item { LoadMoreButton(loadingMore, onLoadMore) }
        }
    }
}

@Composable
private fun HistoryCard(item: TracearrHistoryItem, unknownTitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.mediaTitle ?: unknownTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            item.user?.username?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_user, it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item.startedAt?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_started, it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item.percentComplete?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_watched, it.roundToInt().toString()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RecentlyAddedList(
    items: List<TracearrRecentlyAdded>,
    hasMore: Boolean,
    loadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    if (items.isEmpty()) {
        EmptySection(mokoString(MR.strings.tracearr_no_recently_added))
        return
    }
    val unknown = mokoString(MR.strings.tracearr_label_unknown_title)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id ?: it.hashCode().toString() }) { item ->
            RecentlyAddedCard(item, unknown)
        }
        if (hasMore) {
            item { LoadMoreButton(loadingMore, onLoadMore) }
        }
    }
}

@Composable
private fun RecentlyAddedCard(item: TracearrRecentlyAdded, unknownTitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title ?: unknownTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            item.libraryId?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_library, it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item.addedAt?.let {
                Text(
                    mokoString(MR.strings.tracearr_label_added, it),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LoadMoreButton(loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onClick, enabled = !loading) {
            Text(mokoString(MR.strings.tracearr_load_more))
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
