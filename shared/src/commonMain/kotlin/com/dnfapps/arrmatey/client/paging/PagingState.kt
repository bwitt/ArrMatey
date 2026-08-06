package com.dnfapps.arrmatey.client.paging

sealed class PagingState<out T> {
    data object Initial : PagingState<Nothing>()
    data object Loading : PagingState<Nothing>()
    data object LoadingMore : PagingState<Nothing>()

    data class Success<T>(
        val items: List<T>,
        val currentPage: Int,
        val hasMore: Boolean
    ) : PagingState<T>()

    data class Error(val message: String) : PagingState<Nothing>()
}
