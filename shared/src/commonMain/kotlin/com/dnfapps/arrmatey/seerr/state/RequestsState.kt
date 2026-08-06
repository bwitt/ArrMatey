package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.networking.ErrorType

sealed interface RequestsState {
    object Initial: RequestsState
    object Loading: RequestsState
    data class Success(val items: List<Any>): RequestsState
    data class Error(val message: String?, val type: ErrorType = ErrorType.Http): RequestsState
}