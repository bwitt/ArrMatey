package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class InstanceManagerClientLifecycleTest {
    private val instances = MutableStateFlow<List<Instance>>(emptyList())
    private val createdClients = mutableListOf<HttpClient>()

    private val manager =
        InstanceManager(
            instanceRepository =
            mockk<InstanceRepository>(relaxed = true) {
                every { observeAllInstances() } returns instances
            },
            httpClientFactory =
            mockk<HttpClientFactory> {
                every { create(any()) } answers {
                    HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }).also { createdClients += it }
                }
            },
            credentialMigrationUseCase = mockk(relaxed = true),
            logger = mockk(relaxed = true),
            mokoStrings = mockk(relaxed = true),
        )

    @AfterTest
    fun tearDown() {
        manager.cleanup()
    }

    private fun radarr(
        selected: Boolean = false,
        url: String = "http://localhost:7878",
    ) = Instance(
        id = 1L,
        type = InstanceType.Radarr,
        label = "Radarr",
        url = url,
        apiKey = EncryptedString("key"),
        selected = selected,
    )

    private val HttpClient.isOpen: Boolean
        get() = coroutineContext[Job]!!.isActive

    private suspend fun awaitRepository(predicate: (InstanceScopedRepository?) -> Boolean): InstanceScopedRepository? = withTimeout(5.seconds) {
        manager.instanceRepositories.first { predicate(it[1L]) }[1L]
    }

    @Test
    fun `selecting an instance reuses its open client`() = runBlocking {
        instances.value = listOf(radarr(selected = false))
        val first = awaitRepository { it != null }

        instances.value = listOf(radarr(selected = true))
        val second = awaitRepository { it?.instance?.selected == true }

        assertNotSame(first, second)
        assertEquals(1, createdClients.size)
        assertTrue(createdClients.single().isOpen)
    }

    @Test
    fun `changing connection settings closes the replaced client`() = runBlocking {
        instances.value = listOf(radarr())
        awaitRepository { it != null }

        instances.value = listOf(radarr(url = "http://radarr.lan:7878"))
        awaitRepository { it?.instance?.url == "http://radarr.lan:7878" }

        assertEquals(2, createdClients.size)
        assertFalse(createdClients[0].isOpen)
        assertTrue(createdClients[1].isOpen)
    }

    @Test
    fun `deleting an instance closes its client`() = runBlocking {
        instances.value = listOf(radarr())
        awaitRepository { it != null }

        instances.value = emptyList()
        awaitRepository { it == null }

        assertFalse(createdClients.single().isOpen)
    }
}
