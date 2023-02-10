package com.like.chengdu.socket.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.TimeUnit

/**
 * 实现了重连的客户端
 */
class NettyClient(private val host: String, private val port: Int) {
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

    init {
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .handler(channelInitializer)
    }

    fun connect() {
        println("尝试连接服务器...")
        //连接服务器端
        val cf: ChannelFuture = bootstrap.connect(host, port)
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
        //对通道关闭进行监听
        cf.channel().closeFuture().sync()
    }

}
