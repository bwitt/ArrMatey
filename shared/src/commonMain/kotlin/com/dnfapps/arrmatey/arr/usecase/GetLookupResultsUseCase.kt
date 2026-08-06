package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.state.ArrLibrary
import com.dnfapps.networking.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetLookupResultsUseCase(
    private val instanceManager: InstanceManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(type: InstanceType): Flow<ArrLibrary> =
        instanceManager.getSelectedArrRepository(type)
            .filterNotNull()
            .flatMapLatest { repository ->
                repository.lookupResults.map { result ->
                    when (result) {
                        null -> ArrLibrary.Initial
                        is NetworkResult.Loading -> ArrLibrary.Loading
                        is NetworkResult.Error -> ArrLibrary.Error(result.message ?: "")
                        is NetworkResult.Success<*> ->
                            ArrLibrary.Success(items = result.data as List<ArrMedia>)
                    }
                }
            }
}