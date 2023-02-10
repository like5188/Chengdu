package com.like.chengdu.socket.client

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil

class NettyClientHandler(private val nettyClient: NettyClient) : ChannelHandlerAdapter() {
    /**
     * 当客户端连接服务器完成就会触发该方法
     *
     * @param ctx
     * @throws Exception
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        val buf = Unpooled.copiedBuffer("Hello Server 到底".toByteArray(CharsetUtil.UTF_8))
        ctx.writeAndFlush(buf)
    }

    // channel 处于不活动状态时调用
    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("与服务器的连接已经断开！")
        nettyClient.connect()
    }

    //当通道有读取事件时会触发，即服务端发送数据给客户端
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val str = (msg as ByteBuf).toString(CharsetUtil.UTF_8)
        nettyClient.onMessageReceived.invoke(str)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
