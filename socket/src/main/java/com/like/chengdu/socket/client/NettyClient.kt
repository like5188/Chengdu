package com.like.chengdu.socket.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.TimeUnit

/**
 * Netty客户端
 *
 * @param connectTimeoutMillis      连接超时时长，默认10000毫秒
 * @param reconnectIntervalMillis   两次自动重连之间时间间隔，默认3000毫秒
 * @param onConnected               连接成功回调
 * @param onDisConnected            连接失败回调
 * @param onMessageReceived         接收到了服务器发来的消息的回调
 */
class NettyClient(
    private val connectTimeoutMillis: Int = 10000,
    private val reconnectIntervalMillis: Long = 3000,
    private val onConnected: (() -> Unit)? = null,
    val onDisConnected: (() -> Unit)? = null,
    val onMessageReceived: (Msg) -> Unit
) {
    private lateinit var bootstrap: Bootstrap
    private lateinit var eventLoopGroup: EventLoopGroup
    private val channelInitializer: ChannelInitializer<SocketChannel> by lazy {
        object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                with(ch.pipeline()) {
                    addLast(NettyClientHandler(this@NettyClient))
                }
            }
        }
    }
    private var connectFlag = -1

    /**
     * @param host  服务器地址
     * @param port  服务器端口
     */
    @Synchronized
    fun connect(host: String?, port: Int?) {
        if (connectFlag == 0 || host.isNullOrEmpty() || port == null || port > 65535 || port <= 0) {
            return
        }
        connectFlag = 0// 允许自动重连
        bootstrap = Bootstrap()
        eventLoopGroup = NioEventLoopGroup()
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(host, port)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
            .handler(channelInitializer)
        reConnect()
    }

    internal fun reConnect() {
        if (connectFlag != 0) {
            return
        }
        println("尝试连接服务器...")
        //异步连接服务器端
        val cf: ChannelFuture = bootstrap.connect()
        cf.addListener(ChannelFutureListener { future ->
            if (!future.isSuccess) {// 服务端连接失败
                onDisConnected?.invoke()
                // 延迟自动重连
                future.channel().eventLoop().schedule({
                    reConnect()
                }, reconnectIntervalMillis, TimeUnit.MILLISECONDS)
            } else {// 服务端连接成功
                onConnected?.invoke()
            }
        })
    }

    @Synchronized
    fun disconnect() {
        if (connectFlag == 0 && ::eventLoopGroup.isInitialized) {
            connectFlag = 1// 断开连接后，不允许自动重连
            eventLoopGroup.shutdownGracefully()
        }
    }

}
