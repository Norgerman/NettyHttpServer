@file:JvmName("HttpRequestExtensions")

package com.norgerman.server.internal.extensions

import io.netty.handler.codec.http.*

/**
 * Created by Norgerman on 4/2/2016.
 * HttpRequestExtensions.kt
 */

internal fun HttpRequest.isKeepAlive() = HttpHeaders.isKeepAlive(this);