package com.norgerman.server.internal

import com.norgerman.server.internal.extensions.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import java.io.File
import java.util.*


/**
 * Created by Norgerman on 4/2/2016.
 * HttpHandler.kt
 */
class HttpHandler : SimpleChannelInboundHandler<FullHttpRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest) {
        println("${msg.method} ${msg.uri}");
        println("version: ${msg.protocolVersion}");
        println("Headers: ")
        with(msg.headers()) {
            forEach { e -> println("${e.key}: ${e.value}") }
        }
        if (msg.content().isReadable) {
            println("Body: ")
            with(msg.content()) {
                val lenToRead = readableBytes();
                val bytes = ByteArray(lenToRead);
                readBytes(bytes)
                println(String(bytes, Charsets.UTF_8));
            }
        }
        val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        with(response.headers()) {
            add("Vary", "Accept-Encoding");
            add("Content-Type", "text/plain; charset=utf-8");
        }
        response.setKeepAlive(msg.isKeepAlive());
        HttpHeaders.setDate(response, Date(System.currentTimeMillis()));
        val file = File("./pom.xml");
        val offset = 12L;
        //response.setContentRange(1, 1001, file.length());
        //response.status = HttpResponseStatus.PARTIAL_CONTENT;
        response.setTransferEncodingChunked();
        val message = "Hello World!".toByteArray();
        ctx.write(response);

        val stream = NettyStream(msg, ctx);

        stream.use {
            stream.write(message);
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace();
        ctx.close();
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
        super.channelReadComplete(ctx)
    }
}