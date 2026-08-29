package com.dnfapps.arrmatey.tracearr.api.client

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

class TracearrClientTest {

    private val fakeInstance = Instance(
        id = 1,
        label = "Test Tracearr",
        url = "https://tracearr.example.com",
        apiKey = EncryptedString("trr_pub_secret"),
        type = InstanceType.Tracearr,
        enabled = true
    )

    private fun mockHttpClient(
        onRequest: (path: String, query: String) -> Pair<String, HttpStatusCode> = { _, _ -> "{}" to HttpStatusCode.OK }
    ): HttpClient {
        val engine = MockEngine { request ->
            val (body, status) = onRequest(request.url.encodedPath, request.url.encodedQuery)
            respond(
                content = body,
                status = status,
                headers = headersOf("Content-Type", "application/json")
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        }
    }

    @Test
    fun getStreamsHitsPublicV2StreamsAndReturnsEnvelope() = runTest {
        var seenPath: String? = null
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { path, _ ->
                seenPath = path
                """{"data":[{"id":"s1","media_title":"Inception","state":"playing","progress_ms":10000,"duration_ms":100000}],"summary":{"total":1,"transcodes":0}}""" to HttpStatusCode.OK
            }
        )

        val result = client.getStreams()

        assertEquals("/api/v2/public/streams", seenPath)
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data.data.size)
        assertEquals("Inception", result.data.data[0].mediaTitle)
        assertEquals(1, result.data.summary?.total)
    }

    @Test
    fun getStreamsForwardsServerIdFilter() = runTest {
        var seenQuery: String? = null
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { _, query ->
                seenQuery = query
                """{"data":[],"summary":{"total":0,"transcodes":0}}""" to HttpStatusCode.OK
            }
        )

        client.getStreams(serverId = "srv-42")

        assertTrue(seenQuery!!.contains("server_id=srv-42"))
    }

    @Test
    fun getHistoryEncodesCursorAndPageSize() = runTest {
        var seenQuery: String? = null
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { _, query ->
                seenQuery = query
                """{"data":[],"meta":{"nextCursor":"next-42"}}""" to HttpStatusCode.OK
            }
        )

        val result = client.getHistory(cursor = "abc", pageSize = 50)

        val query = seenQuery!!
        assertTrue(query.contains("pageSize=50"))
        assertTrue(query.contains("cursor=abc"))
        assertTrue(result is NetworkResult.Success)
        assertEquals("next-42", result.data.meta.nextCursor)
    }

    @Test
    fun getHistoryOmitsCursorWhenNull() = runTest {
        var seenQuery: String? = null
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { _, query ->
                seenQuery = query
                """{"data":[],"meta":{"nextCursor":null}}""" to HttpStatusCode.OK
            }
        )

        client.getHistory(cursor = null)

        assertTrue(seenQuery!!.contains("pageSize="))
        assertTrue(!seenQuery.contains("cursor="))
    }

    @Test
    fun getLibrariesReturnsBareArray() = runTest {
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { _, _ ->
                """[{"server_id":"srv-1","library_id":"lib-a","item_count":42}]""" to HttpStatusCode.OK
            }
        )

        val result = client.getLibraries()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data.size)
        assertEquals("lib-a", result.data[0].libraryId)
        assertEquals(42L, result.data[0].itemCount)
    }

    @Test
    fun getRecentlyAddedTrimsTrailingSlashOnInstanceUrl() = runTest {
        var seenPath: String? = null
        val trailing = fakeInstance.copy(url = "https://tracearr.example.com/")
        val client = TracearrClient(
            trailing,
            mockHttpClient { path, _ ->
                seenPath = path
                """{"data":[],"meta":{"nextCursor":null}}""" to HttpStatusCode.OK
            }
        )

        client.getRecentlyAdded()

        assertEquals("/api/v2/public/recently-added", seenPath)
    }

    @Test
    fun testConnectionHitsDocsEndpoint() = runTest {
        var seenPath: String? = null
        val client = TracearrClient(
            fakeInstance,
            mockHttpClient { path, _ ->
                seenPath = path
                "{}" to HttpStatusCode.OK
            }
        )

        val result = client.testConnection()

        assertEquals("/api/v2/public/docs", seenPath)
        assertTrue(result is NetworkResult.Success)
    }
}
