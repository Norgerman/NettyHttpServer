@file:JvmName("ChannelFeatureExtensions")

package com.norgerman.server.internal.extensions

import io.netty.channel.*

/**
 * Created by Norgerman on 4/2/2016.
 * ChannelFeatureExtensions.kt
 */

internal fun ChannelFuture.scheduleClose(keepAlive: Boolean) {
    if (!keepAlive) {
        addListener(ChannelFutureListener.CLOSE);
    }
}