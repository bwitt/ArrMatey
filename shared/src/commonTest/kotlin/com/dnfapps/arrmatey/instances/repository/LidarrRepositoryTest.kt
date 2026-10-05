package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import com.dnfapps.arrmatey.arr.api.model.HistoryEventType
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LidarrRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 3,
            label = "Test Lidarr",
            url = "http://localhost:8686",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Lidarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testGetArtistAlbums() = runTest {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content =
                    """
                            [{
                                "id": 50,
                                "artistId": 2,
                                "foreignAlbumId":
                                "foreign-123",
                                "anyReleaseOk": true,
                                "profileId": 1,
                                "duration": 3600,
                                "title": "Greatest Hits",
                                "monitored": true
                            }]
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }
        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }
        val repository = LidarrRepository(fakeInstance, httpClient, fakeLogger)

        repository.getArtistAlbums(artistId = 2)

        assertNotNull(repository.artistAlbums.value[2])
        assertEquals(1, repository.artistAlbums.value[2]?.size)
        assertEquals(
            "Greatest Hits",
            repository.artistAlbums.value[2]
                ?.first()
                ?.title,
        )
    }

    @Test
    fun testGetItemHistoryIsScopedToArtist() = runTest {
        val requests = mutableListOf<Url>()
        val repository = LidarrRepository(fakeInstance, historyHttpClient(requests, listOf("grabbed")), fakeLogger)

        repository.getItemHistory(itemId = 2)

        val url = requests.single()
        assertEquals("/api/v1/history/artist", url.encodedPath)
        assertEquals("2", url.parameters["artistId"])
        assertNull(url.parameters["albumId"])
        assertEquals(1, repository.observeItemHistory(2).first().size)
    }

    @Test
    fun testGetItemHistoryDecodesAllLidarrEventTypes() = runTest {
        val lidarrEvents =
            listOf(
                "grabbed",
                "artistFolderImported",
                "trackFileImported",
                "downloadFailed",
                "trackFileDeleted",
                "trackFileRenamed",
                "albumImportIncomplete",
                "downloadImported",
                "trackFileRetagged",
                "downloadIgnored",
            )
        val repository = LidarrRepository(fakeInstance, historyHttpClient(mutableListOf(), lidarrEvents), fakeLogger)

        repository.getItemHistory(itemId = 2)

        val history = repository.observeItemHistory(2).first()
        assertEquals(lidarrEvents.size, history.size)
        assertTrue(history.none { it.eventType == HistoryEventType.Unknown })
    }

    private fun historyHttpClient(
        requests: MutableList<Url>,
        eventTypes: List<String>,
    ): HttpClient = HttpClient(
        MockEngine { request ->
            requests += request.url
            val records =
                eventTypes.mapIndexed { index, eventType ->
                    """
                    {
                        "id": $index,
                        "eventType": "$eventType",
                        "date": "2026-01-01T00:00:00Z",
                        "quality": {"quality": {"id": 1, "name": "FLAC"}, "revision": {"version": 1, "real": 0, "isRepack": false}},
                        "artistId": 2,
                        "albumId": 50
                    }
                    """.trimIndent()
                }
            respond(
                content = records.joinToString(prefix = "[", postfix = "]"),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json"),
            )
        },
    ) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    serializersModule =
                        SerializersModule {
                            contextual(Instant::class, ListenarrInstantSerializer)
                        }
                },
            )
        }
    }
}
