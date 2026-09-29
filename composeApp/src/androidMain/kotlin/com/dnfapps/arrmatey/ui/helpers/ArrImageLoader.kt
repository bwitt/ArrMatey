package com.dnfapps.arrmatey.ui.helpers

import android.content.Context
import coil3.EventListener
import coil3.ImageLoader
import coil3.intercept.Interceptor
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.NullRequestDataException
import coil3.request.crossfade
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import dev.shivathapaa.logger.api.Logger

class ArrImageLoader(
    private val context: Context,
    private val instanceManager: InstanceManager,
    private val logger: Logger,
) {
    private val apiKeyRegex = Regex("(apikey=)[^&]*", RegexOption.IGNORE_CASE)

    private val errorLogger =
        object : EventListener() {
            override fun onError(
                request: ImageRequest,
                result: ErrorResult,
            ) {
                if (result.throwable is NullRequestDataException) return
                val url = request.data.toString().replace(apiKeyRegex, "$1***")
                logger.error(result.throwable) { "Error loading image $url" }
            }
        }

    private val apiKeyInterceptor =
        Interceptor { chain ->
            val request = chain.request
            val url = request.data.toString()

            val repository =
                instanceManager.getAllRepositories().find { repository ->
                    url.startsWith(repository.instance.url) ||
                        url.startsWith(repository.instance.getEffectiveBaseUrl())
                }
            val instance = repository?.instance

            val newRequest =
                if (instance != null) {
                    val headersBuilder = NetworkHeaders.Builder().set("Accept", "image/*")

                    val authenticatedUrl =
                        if (instance.type == InstanceType.Tracearr) {
                            headersBuilder.set("Authorization", "Bearer ${instance.apiKey.value}")
                            url
                        } else {
                            headersBuilder.set("X-Api-Key", instance.apiKey.value)
                            if (!url.contains("apikey=")) {
                                val separator = if (url.contains("?")) "&" else "?"
                                "$url${separator}apikey=${instance.apiKey.value}"
                            } else {
                                url
                            }
                        }

                    request
                        .newBuilder()
                        .data(authenticatedUrl)
                        .httpHeaders(headersBuilder.build())
                        .build()
                } else {
                    request
                }

            val result =
                chain
                    .withRequest(newRequest)
                    .proceed()
            result
        }

    val imageLoader: ImageLoader by lazy {
        ImageLoader
            .Builder(context)
            .components {
                add(apiKeyInterceptor)
            }.eventListener(errorLogger)
            .crossfade(true)
            .build()
    }
}
