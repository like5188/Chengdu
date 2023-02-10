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
    private val host: String,
    private val port: Int,
    private val connectTimeoutMillis: Int = 10000,
    private val reconnectIntervalMillis: Long = 3000,
    val onMessageReceived: (String) -> Unit
) {
    private lateinit var bootstrap: Bootstrap
    private lateinit var eventLoopGroup: EventLoopGroup
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
    private var connectStatus = -1

    @Synchronized
    fun connect() {
        if (connectStatus == 0) {
            return
        }
        connectStatus = 0
        bootstrap = Bootstrap()
        eventLoopGroup = NioEventLoopGroup()
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(host, port)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
            .handler(channelInitializer)
        doConnect()
    }

    internal fun doConnect() {
        if (connectStatus != 0) {
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
    fun disconnect() {
        if (connectStatus == 0 && ::eventLoopGroup.isInitialized) {
            connectStatus = 1
            eventLoopGroup.shutdownGracefully()
        }
    }

}
