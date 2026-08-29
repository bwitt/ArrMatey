package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.TracearrInstanceRepository
import kotlinx.coroutines.flow.Flow

class GetTracearrInstanceRepositoryUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): TracearrInstanceRepository? =
        instanceManager.getTracearrRepository(instanceId)

    fun observeSelected(): Flow<TracearrInstanceRepository?> =
        instanceManager.getSelectedTracearrRepository()
}
