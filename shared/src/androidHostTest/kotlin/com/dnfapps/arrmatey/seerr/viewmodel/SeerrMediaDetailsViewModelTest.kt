package com.dnfapps.arrmatey.seerr.viewmodel

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SeerrMediaDetailsViewModelTest {
    private class FakeRepo {
        val users = MutableStateFlow<List<SeerrUser>>(emptyList())
        val loggedInUser = MutableStateFlow<SeerrUser?>(null)
        val radarrServices = MutableStateFlow<List<Service>>(emptyList())
        val sonarrServices = MutableStateFlow<List<Service>>(emptyList())
        val repository: SeerrInstanceRepository =
            mockk(relaxed = true) {
                every { users } returns this@FakeRepo.users
                every { loggedInUser } returns this@FakeRepo.loggedInUser
                every { radarrServices } returns this@FakeRepo.radarrServices
                every { sonarrServices } returns this@FakeRepo.sonarrServices
            }
    }

    private val selectedRepository = MutableStateFlow<SeerrInstanceRepository?>(null)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SeerrMediaDetailsViewModel(
        tmdbId = 1L,
        mediaType = RequestType.Movie,
        getSeerrInstanceRepositoryUseCase =
        mockk(relaxed = true) {
            every { observeSelected() } returns selectedRepository
        },
        getSeerrMediaDetailsUseCase =
        mockk(relaxed = true) {
            every { this@mockk.invoke(any(), any(), any()) } returns emptyFlow()
        },
        setRequestApprovalStatusUseCase = mockk(relaxed = true),
        cancelRequestUseCase = mockk(relaxed = true),
        getSeerrTvRatingsUseCase = mockk(relaxed = true) { every { this@mockk.invoke(any()) } returns emptyFlow() },
        getSeerrMovieRatingsUseCase = mockk(relaxed = true) { every { this@mockk.invoke(any()) } returns emptyFlow() },
        submitIssueUseCase = mockk(relaxed = true),
        submitRequestUseCase = mockk(relaxed = true),
        getPersonCreditsUseCase = mockk(relaxed = true),
    )

    private fun FakeRepo.subscriptionCounts() = listOf(
        users.subscriptionCount.value,
        loggedInUser.subscriptionCount.value,
        radarrServices.subscriptionCount.value,
        sonarrServices.subscriptionCount.value,
    )

    @Test
    fun `refreshing details does not stack repository collectors`() {
        val repo = FakeRepo()
        selectedRepository.value = repo.repository
        val viewModel = createViewModel()

        repeat(3) { viewModel.refreshDetails() }

        assertEquals(listOf(1, 1, 1, 1), repo.subscriptionCounts())
    }

    @Test
    fun `switching instance stops collecting from the previous repository`() {
        val first = FakeRepo()
        val second = FakeRepo()
        selectedRepository.value = first.repository
        createViewModel()

        selectedRepository.value = second.repository

        assertEquals(listOf(0, 0, 0, 0), first.subscriptionCounts())
        assertEquals(listOf(1, 1, 1, 1), second.subscriptionCounts())
    }
}
