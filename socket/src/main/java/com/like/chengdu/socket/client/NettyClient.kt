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
class NettyClient(host: String, port: Int) {
    private val bootstrap: Bootstrap by lazy {
        Bootstrap()
    }

    //配置客户端的线程组，客户端只有一个线程组
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

    init {
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(host, port)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .handler(channelInitializer)
    }

    fun connect() {
        println("尝试连接服务器...")
        //异步连接服务器端
        val cf: ChannelFuture = bootstrap.connect()
        cf.addListener(ChannelFutureListener { future ->
            if (!future.isSuccess) {
                println("服务端连接失败！")
                // 延迟重新执行连接
                future.channel().eventLoop().schedule({
                    connect()
                }, 3, TimeUnit.SECONDS)
            } else {
                println("服务端连接成功！")
            }
        })
        //同步等待客户端链路关闭
        cf.channel().closeFuture().sync()
    }

}
