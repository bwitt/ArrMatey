package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.arrmatey.extensions.withoutStaleLibraryId
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetLookupResultsUseCase(
    private val instanceManager: InstanceManager,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        type: InstanceType,
        instanceId: Long? = null,
    ): Flow<ArrLibrary> {
        val repoFlow =
            if (instanceId != null) {
                flowOf(instanceManager.getArrRepository(instanceId))
            } else {
                instanceManager.getSelectedArrRepository(type)
            }

        return repoFlow
            .filterNotNull()
            .flatMapLatest { repository ->
                combine(repository.lookupResults, repository.library) { result, library ->
                    when (result) {
                        null -> ArrLibrary.Initial
                        is NetworkResult.Loading -> ArrLibrary.Loading
                        is NetworkResult.Error -> ArrLibrary.Error(result.message ?: "")
                        is NetworkResult.Success -> {
                            val libraryIds = library?.asSuccess()?.data?.mapNotNullTo(HashSet()) { it.id }
                            val items = libraryIds?.let { ids -> result.data.map { it.withoutStaleLibraryId(ids) } } ?: result.data
                            ArrLibrary.Success(items = items)
                        }
                    }
                }
            }
    }
}
