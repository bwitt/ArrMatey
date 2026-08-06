package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.networking.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance

sealed interface InstanceScopedRepository {
    val instance: Instance

    suspend fun testConnection(): NetworkResult<Unit>
}