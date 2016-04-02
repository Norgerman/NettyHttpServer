package com.norgerman.server

import com.norgerman.server.internal.HttpHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * Created by Norgerman on 4/2/2016.
 * WebApplication.kt
 */
class WebApplication(port: Int) {
    private var netHost = "localhost";

    val port = port;
    val host: String
        get() = netHost;

    constructor(host: String, port: Int) : this(port) {
        this.netHost = host;
    }

    fun run() {
        val bossGroup: EventLoopGroup = NioEventLoopGroup();
        val workerGroup: EventLoopGroup = NioEventLoopGroup();

        try {
            val bootStrap: ServerBootstrap = ServerBootstrap().apply {
                group(bossGroup, workerGroup);
                channel(NioServerSocketChannel::class.java);
                childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        with(ch.pipeline()) {
                            addLast(HttpServerCodec());
                            addLast(HttpObjectAggregator(1048576));
                            addLast(ChunkedWriteHandler());
                            addLast(HttpHandler());
                        }
                    }
                });
                option(ChannelOption.SO_BACKLOG, 128);
                childOption(ChannelOption.SO_KEEPALIVE, true);
            };

            println("starting server...");

            val future: ChannelFuture = bootStrap.bind(host, port).sync();

            println("server started listening on $host:$port");

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}