package com.norgerman.server.internal

import com.norgerman.server.internal.extensions.*
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.handler.stream.*
import java.io.*

/**
 * Created by Norgerman on 4/2/2016.
 * NettyStream
 * Reference to https://github.com/Kotlin/ktor/blob/master/ktor-hosts/ktor-netty/src/org/jetbrains/ktor/netty/NettyAsyncStream.kt
 */
internal class NettyStream(val request: HttpRequest, val context: ChannelHandlerContext) : OutputStream() {
    private val buffer = context.alloc().buffer(8192)

    private var lastContentWritten = false;

    init {

    }

    override fun write(b: Int) {
        require(lastContentWritten == false) { "You can't write after the last chunk was written" };

        buffer.writeByte(b);
        if (buffer.writableBytes() == 0) {
            flush();
        }
    }

    tailrec
    override fun write(b: ByteArray, off: Int, len: Int) {
        require(lastContentWritten == false) { "You can't write after the last chunk was written" };

        val toWrite = Math.min(len, buffer.writableBytes());
        if (toWrite > 0) {
            buffer.writeBytes(b, off, toWrite);
            if (buffer.writableBytes() == 0) {
                flush();
            }
            if (toWrite < len) {
                write(b, off + toWrite, len - toWrite);
            }
        }
    }

    fun write(fileName: String) {
        write(File(fileName));
    }

    fun write(file: File) {
        write(file, 0, file.length());
    }

    fun write(file: File, position: Long, count: Long) {
        flush();
        context.write(DefaultFileRegion(file, position, count));
    }

    fun write(stream: InputStream) {
        flush();
        context.write(HttpChunkedInput(ChunkedStream(stream.buffered())));
        lastContentWritten = true;
    }

    override fun flush() {
        if (!lastContentWritten && buffer.readableBytes() > 0) {
            context.writeAndFlush(DefaultHttpContent(buffer.copy()));
            buffer.writerIndex(0);
        }

    }

    override fun close() {
        flush();
        finish();
        buffer.release();
    }

    private fun finish() {
        if (!lastContentWritten) {
            context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).scheduleClose(request.isKeepAlive());
            lastContentWritten = true;
        } else if (!request.isKeepAlive()) {
            context.close();
        }
    }
}