package com.like.chengdu.socket.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.TimeUnit

/**
 * 实现了重连的客户端
 */
class NettyClient(
    host: String,
    port: Int,
    connectTimeoutMillis: Int = 10000,
    private val reconnectIntervalMillis: Long = 3000,
    val onMessageReceived: (String) -> Unit
) {
    private val bootstrap: Bootstrap by lazy {
        Bootstrap()
    }
    private val eventLoopGroup: EventLoopGroup by lazy {
        NioEventLoopGroup()
    }
    private val channelInitializer: ChannelInitializer<SocketChannel> by lazy {
        object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: SocketChannel) {
                with(ch.pipeline()) {
                    addLast(NettyClientHandler(this@NettyClient))
                }
            }
        }
    }
    private var isManuallyClose = false

    init {
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(host, port)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
            .handler(channelInitializer)
    }

    @Synchronized
    fun connect() {
        if (isManuallyClose) {
            return
        }
        println("尝试连接服务器...")
        //异步连接服务器端
        val cf: ChannelFuture = bootstrap.connect()
        cf.addListener(ChannelFutureListener { future ->
            if (!future.isSuccess) {
                println("服务端连接失败！")
                // 延迟重连
                future.channel().eventLoop().schedule({
                    connect()
                }, reconnectIntervalMillis, TimeUnit.MILLISECONDS)
            } else {
                println("服务端连接成功！")
            }
        })
    }

    @Synchronized
    fun disConnect() {
        isManuallyClose = true
        eventLoopGroup.shutdownGracefully()
    }

}
