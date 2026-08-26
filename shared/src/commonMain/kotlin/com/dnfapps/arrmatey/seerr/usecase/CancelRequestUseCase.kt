package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.networking.NetworkResult
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository

class CancelRequestUseCase {
    suspend operator fun invoke(requestId: Long, repository: SeerrInstanceRepository): NetworkResult<Unit> =
        repository.deleteRequest(requestId)
}