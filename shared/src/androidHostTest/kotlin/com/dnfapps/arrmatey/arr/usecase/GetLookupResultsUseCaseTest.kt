package com.dnfapps.arrmatey.arr.usecase

import app.cash.turbine.test
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.networking.NetworkResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetLookupResultsUseCaseTest {
    private val owned = series(id = 99L)

    @Test
    fun deletedItemLosesItsLibraryId() = runTest {
        val library = MutableStateFlow<NetworkResult<List<ArrMedia>>?>(NetworkResult.Success(listOf(owned)))
        val useCase = GetLookupResultsUseCase(managerWith(library))

        useCase(InstanceType.Sonarr, instanceId = 1L).test {
            assertEquals(99L, awaitItems().single().id)

            library.value = NetworkResult.Success(emptyList())

            assertNull(awaitItems().single().id)
        }
    }

    @Test
    fun keepsIdWhileLibraryIsNotLoaded() = runTest {
        val library = MutableStateFlow<NetworkResult<List<ArrMedia>>?>(null)
        val useCase = GetLookupResultsUseCase(managerWith(library))

        useCase(InstanceType.Sonarr, instanceId = 1L).test {
            assertEquals(99L, awaitItems().single().id)
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<ArrLibrary>.awaitItems() = (awaitItem() as ArrLibrary.Success).items

    private fun managerWith(library: MutableStateFlow<NetworkResult<List<ArrMedia>>?>): InstanceManager {
        val repo =
            mockk<ArrInstanceRepository> {
                every { lookupResults } returns MutableStateFlow(NetworkResult.Success(listOf<ArrMedia>(owned)))
                every { this@mockk.library } returns library
            }
        return mockk { every { getArrRepository(1L) } returns repo }
    }

    private fun series(id: Long?) = ArrSeries(
        id = id,
        title = "Owned",
        originalLanguage = Language(1, "English"),
        year = 2020,
        qualityProfileId = 1,
        monitored = true,
        runtime = 45,
        status = MediaStatus.Ended,
        ended = true,
        seasonFolder = false,
        monitorNewItems = MonitorNewItems.All,
        useSceneNumbering = false,
        tvdbId = 1,
        tmdbId = 1L,
        seriesType = SeriesType.Standard,
    )
}
