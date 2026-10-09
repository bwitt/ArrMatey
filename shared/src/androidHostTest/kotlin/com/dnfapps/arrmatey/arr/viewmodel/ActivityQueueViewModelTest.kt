package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModelStore
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.arr.usecase.GetActivityTasksUseCase
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class ActivityQueueViewModelTest {
    private val activityQueueService =
        mockk<ActivityQueueService>(relaxed = true) {
            every { allHistory } returns MutableStateFlow(emptyList())
            every { allBlocklist } returns MutableStateFlow(emptyList())
            every { allMissing } returns MutableStateFlow(emptyList())
            every { allCutoff } returns MutableStateFlow(emptyList())
            every { isPolling } returns MutableStateFlow(false)
            every { hasLoaded } returns MutableStateFlow(false)
            every { isHistoryLoading } returns MutableStateFlow(false)
            every { hasHistoryLoaded } returns MutableStateFlow(false)
            every { isBlocklistLoading } returns MutableStateFlow(false)
            every { hasBlocklistLoaded } returns MutableStateFlow(false)
            every { isMissingLoading } returns MutableStateFlow(false)
            every { hasMissingLoaded } returns MutableStateFlow(false)
            every { isCutoffLoading } returns MutableStateFlow(false)
            every { hasCutoffLoaded } returns MutableStateFlow(false)
        }

    private fun createViewModel() = ActivityQueueViewModel(
        activityQueueService = activityQueueService,
        getActivityTasksUseCase =
        mockk<GetActivityTasksUseCase> {
            every { this@mockk.invoke() } returns flowOf(emptyList())
            every { getTasksWithIssues() } returns flowOf(0)
        },
        instanceRepository =
        mockk<InstanceRepository> {
            every { observeAllInstances() } returns flowOf(emptyList())
        },
        deleteQueueItemUseCase = mockk(),
        instanceManager =
        mockk<InstanceManager> {
            every { instanceRepositories } returns MutableStateFlow(emptyMap())
        },
        executeArrCommandUseCase = mockk(),
    )

    @Test
    fun testInitStartsPolling() {
        createViewModel()

        verify { activityQueueService.startPolling() }
    }

    @Test
    fun testClearingViewModelDoesNotStopSharedPolling() {
        val store = ViewModelStore()
        store.put("activity", createViewModel())

        store.clear()

        verify(exactly = 0) { activityQueueService.stopPolling() }
        verify(exactly = 0) { activityQueueService.cleanup() }
    }
}
