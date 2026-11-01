package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivityQueueServiceTest {
    private val instanceManager: InstanceManager =
        mockk(relaxed = true) {
            every { instanceRepositories } returns MutableStateFlow(emptyMap())
        }
    private val service = ActivityQueueService(instanceManager)

    @AfterTest
    fun tearDown() {
        service.cleanup()
    }

    @Test
    fun `polling continues until the last attached view model detaches`() {
        service.attachViewModel()
        service.attachViewModel()
        assertTrue(service.isPollingActive)

        service.detachViewModel()
        assertTrue(service.isPollingActive)

        service.detachViewModel()
        assertFalse(service.isPollingActive)
    }

    @Test
    fun `extra detach does not go negative and block the next stop`() {
        service.detachViewModel()
        service.attachViewModel()
        assertTrue(service.isPollingActive)

        service.detachViewModel()
        assertFalse(service.isPollingActive)
    }
}
