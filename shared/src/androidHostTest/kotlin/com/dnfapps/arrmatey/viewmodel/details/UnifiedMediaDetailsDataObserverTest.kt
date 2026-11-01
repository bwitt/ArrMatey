package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.model.AddSheetUiState
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsDataObserverTest {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val presences = MutableStateFlow<Map<Long, ArrMedia?>>(emptyMap())
    private val activeArrRepo = MutableStateFlow<ArrInstanceRepository?>(null)
    private val allArrRepos = MutableStateFlow<List<ArrInstanceRepository>>(emptyList())
    private val seerrRepo = MutableStateFlow<SeerrInstanceRepository?>(null)
    private val bazarrRepo = MutableStateFlow<BazarrInstanceRepository?>(null)
    private val tracearrRepo = MutableStateFlow<TracearrRepository?>(null)

    private val instanceHandler: UnifiedMediaDetailsInstanceHandler =
        mockk(relaxed = true) {
            every { instancePresencesMap } returns presences
            every { activeArrRepoFlow } returns activeArrRepo
            every { allArrReposFlow } returns allArrRepos
            every { seerrRepositoryFlow } returns seerrRepo
            every { bazarrRepositoryFlow } returns bazarrRepo
            every { selectedInstanceId } returns MutableStateFlow(null)
            every { addSheetUiState } returns MutableStateFlow(AddSheetUiState())
        }

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    private fun createObserver(detailsFlow: Flow<UnifiedMediaDetailsUiState> = emptyFlow()) = UnifiedMediaDetailsDataObserver(
        scope = scope,
        arrId = 1L,
        tmdbId = 1L,
        tvdbId = null,
        resolvedInstanceType = InstanceType.Radarr,
        resolvedRequestType = RequestType.Movie,
        getUnifiedMediaDetailsUseCase =
        mockk {
            every { this@mockk.invoke(any(), any(), any(), any(), any(), any(), any(), any()) } returns detailsFlow
        },
        getInstancePresencesUseCase = mockk(relaxed = true),
        preferencesStore =
        mockk {
            every { combineSeerrArrMedia } returns flowOf(true)
            every { bazarrDetailsIntegration } returns flowOf(false)
            every { tracearrDetailsIntegration } returns flowOf(true)
        },
        instanceHandler = instanceHandler,
        arrActionsHandler = mockk(relaxed = true),
        seerrServiceHandler =
        UnifiedMediaDetailsSeerrServiceHandler(
            scope = scope,
            resolvedRequestType = RequestType.Movie,
            uiStateFlow = emptyFlow(),
            isSeerrConfiguredFlow = flowOf(false),
        ),
        tracearrHandler = UnifiedMediaDetailsTracearrHandler(),
        getTracearrInstanceRepositoryUseCase =
        mockk {
            every { observeSelected() } returns tracearrRepo
        },
        onIsMonitoredUpdated = {},
    )

    private fun subscriptionCounts() = listOf(
        presences.subscriptionCount.value,
        seerrRepo.subscriptionCount.value,
        tracearrRepo.subscriptionCount.value,
    )

    @Test
    fun `re-observing replaces the previous collectors instead of stacking them`() {
        val observer = createObserver()
        val uiState = MutableStateFlow<UnifiedMediaDetailsUiState>(UnifiedMediaDetailsUiState.Initial)

        observer.observeData(uiState)
        val afterFirst = subscriptionCounts()
        observer.observeData(uiState)
        observer.observeData(uiState)

        assertEquals(afterFirst, subscriptionCounts())
    }

    @Test
    fun `repeated success emissions start a single history observer`() {
        val repo: ArrInstanceRepository =
            mockk(relaxed = true) {
                every { instance } returns mockk<Instance>(relaxed = true) { every { id } returns 10L }
                every { qualityProfiles } returns MutableStateFlow(emptyList())
                every { metadataProfiles } returns MutableStateFlow(emptyList())
                every { rootFolders } returns MutableStateFlow(emptyList())
                every { tags } returns MutableStateFlow(emptyList())
                every { addItemStatus } returns MutableStateFlow(OperationStatus.Idle)
                every { editItemStatus } returns MutableStateFlow(OperationStatus.Idle)
                every { observeItemHistory(any()) } returns MutableStateFlow(emptyList())
            }
        activeArrRepo.value = repo
        val success = UnifiedMediaDetailsUiState.Success(arrMedia = MockMedia.Radarr)
        val observer = createObserver(detailsFlow = flowOf(success, success.copy(isMonitored = true), success))

        observer.observeData(MutableStateFlow(UnifiedMediaDetailsUiState.Initial))

        verify(exactly = 1) { repo.observeItemHistory(MockMedia.Radarr.id) }
        coVerify(exactly = 1) { repo.getItemHistory(MockMedia.Radarr.id, any(), any(), any()) }
    }
}
