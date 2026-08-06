package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CancelRequestUseCase {
    suspend operator fun invoke(requestId: Long, repository: SeerrInstanceRepository): NetworkResult<Unit> =
        repository.deleteRequest(requestId)
}