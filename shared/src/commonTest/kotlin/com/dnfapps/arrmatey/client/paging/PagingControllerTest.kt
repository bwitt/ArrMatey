package com.dnfapps.arrmatey.client.paging

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PagingControllerTest {
    private class ControlledSource(
        private val name: String,
    ) : PagingSource<String>() {
        val pending = mutableMapOf<Int, CompletableDeferred<Unit>>()
        val requestedPages = mutableListOf<Int>()

        fun complete(page: Int) = pending.getValue(page).complete(Unit)

        override suspend fun load(page: Int): LoadResult<String> {
            requestedPages += page
            pending.getOrPut(page) { CompletableDeferred() }.await()
            return LoadResult.Page(
                data = listOf("$name-$page"),
                totalItemCount = 10,
                currentPage = page,
                hasNextPage = true,
            )
        }
    }

    @Test
    fun `refresh drops a next page that was loading from the old source`() = runTest(UnconfinedTestDispatcher()) {
        val sources = mutableListOf<ControlledSource>()
        val controller = PagingController(this) { ControlledSource("s${sources.size}").also(sources::add) }

        controller.loadInitialPage()
        sources[0].complete(1)
        controller.loadNextPage()

        controller.refresh()
        sources[1].complete(1)
        sources[0].complete(2)

        assertEquals(listOf("s1-1"), controller.state.value.items)
        assertEquals(1, controller.state.value.currentPage)
    }

    @Test
    fun `next page is only requested once while it is loading`() = runTest {
        val source = ControlledSource("s")
        val controller = PagingController(this) { source }

        controller.loadInitialPage()
        runCurrent()
        source.complete(1)
        runCurrent()
        controller.loadNextPage()
        controller.loadNextPage()
        runCurrent()
        source.complete(2)
        runCurrent()

        assertEquals(listOf(1, 2), source.requestedPages)
        assertEquals(listOf("s-1", "s-2"), controller.state.value.items)
    }
}
