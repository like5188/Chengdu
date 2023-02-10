package com.like.chengdu.socket.client

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * 心跳检测：
 * IdleStateHandler 可以实现心跳功能，当服务器和客户端没有任何读写交互时，
 * 并超过了给定的时间，则会触发用户 handler 的 userEventTriggered 方法。
 * 用户可以在这个方法中尝试向对方发送信息，如果发送失败，则关闭连接。
 */
class HeartBeatHandler : ChannelHandlerAdapter() {
    /**
     * 读取到服务端响应，如果是PONG响应，则打印。如果是非PONG响应，则传递至下一个Handler
     *
     * @param ctx 处理上下文
     * @param msg 消息
     * @throws Exception
     * @author huaijin
     */
    @Throws(Exception::class)
    override
    fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (PONG == msg) {
            println("from heart bean: $msg")
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    /**
     * 用于捕获{@link IdleState#WRITER_IDLE}事件（未在指定时间内向服务器发送数据），然后向<code>Server</code>端发送一个心跳包。
     *
     * @param ctx 处理上下文
     * @param evt 事件
     * @throws Exception
     * @author huaijin
     */
    @Throws(Exception::class)
    override
    fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            when (evt.state()) {
                IdleState.WRITER_IDLE -> sendPing(ctx)
                else -> {}
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

    /**
     * 发送PING消息
     *
     * @param ctx 上下文
     * @author huaijin
     */
    private fun sendPing(ctx: ChannelHandlerContext) {
        println("send heart beat: $PING")
        //发送心跳消息，并在发送失败时关闭该连接
        ctx.writeAndFlush(Unpooled.copiedBuffer((PING + SPLIT).toByteArray())).addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
    }

    companion object {
        /**
         * PING消息
         */
        private const val PING = "0"

        /**
         * PONG消息
         */
        private const val PONG = "1"

        /**
         * 分隔符
         */
        private const val SPLIT = "\$_"
    }
}