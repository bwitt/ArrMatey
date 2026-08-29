package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TracearrInstanceRepositoryTest {

    private val fakeInstance = Instance(
        id = 1,
        label = "Test Tracearr",
        url = "https://tracearr.example.com",
        apiKey = EncryptedString("trr_pub_secret"),
        type = InstanceType.Tracearr,
        enabled = true
    )

    private fun httpClient(vararg responses: String): HttpClient {
        val queue = responses.toMutableList()
        val engine = MockEngine {
            val body = queue.removeAt(0)
            respond(body, HttpStatusCode.OK, headersOf("Content-Type", "application/json"))
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        }
    }

    @Test
    fun refreshStreamsUnwrapsEnvelopeAndPublishesSummary() = runTest {
        val repo = TracearrInstanceRepository(
            fakeInstance,
            httpClient("""{"data":[{"id":"s1","media_title":"Show"}],"summary":{"total":1,"transcodes":0}}""")
        )

        val result = repo.refreshStreams()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data.size)
        val cached = repo.streams.value
        assertTrue(cached is NetworkResult.Success)
        assertEquals("Show", cached.data[0].mediaTitle)
        assertEquals(1, repo.streamSummary.value?.total)
    }

    @Test
    fun refreshHistoryReplacesDataAndCapturesNextCursor() = runTest {
        val repo = TracearrInstanceRepository(
            fakeInstance,
            httpClient("""{"data":[{"id":"h1","media_title":"One"}],"meta":{"nextCursor":"c-2"}}""")
        )

        repo.refreshHistory()

        val cached = repo.history.value
        assertTrue(cached is NetworkResult.Success)
        assertEquals(1, cached.data.size)
        assertEquals("c-2", repo.historyNextCursor.value)
    }

    @Test
    fun loadMoreHistoryAppendsAndAdvancesCursor() = runTest {
        val repo = TracearrInstanceRepository(
            fakeInstance,
            httpClient(
                """{"data":[{"id":"h1","media_title":"One"}],"meta":{"nextCursor":"c-2"}}""",
                """{"data":[{"id":"h2","media_title":"Two"}],"meta":{"nextCursor":null}}"""
            )
        )

        repo.refreshHistory()
        repo.loadMoreHistory()

        val cached = repo.history.value
        assertTrue(cached is NetworkResult.Success)
        assertEquals(listOf("h1", "h2"), cached.data.map { it.id })
        assertNull(repo.historyNextCursor.value)
    }

    @Test
    fun loadMoreHistoryWithoutCursorIsNoop() = runTest {
        val repo = TracearrInstanceRepository(
            fakeInstance,
            httpClient("this should never be requested")
        )

        val result = repo.loadMoreHistory()

        assertTrue(result is NetworkResult.Success)
        assertEquals(0, result.data.size)
        assertNull(repo.history.value)
    }

    @Test
    fun refreshLibrariesStoresList() = runTest {
        val repo = TracearrInstanceRepository(
            fakeInstance,
            httpClient("""[{"server_id":"srv","library_id":"lib","item_count":7}]""")
        )

        repo.refreshLibraries()

        assertEquals(1, repo.libraries.value.size)
        assertEquals("lib", repo.libraries.value[0].libraryId)
        assertEquals(7L, repo.libraries.value[0].itemCount)
    }
}
