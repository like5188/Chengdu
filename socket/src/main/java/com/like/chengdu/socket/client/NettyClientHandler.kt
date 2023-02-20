package com.like.chengdu.socket.client

import com.google.gson.Gson
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil

class NettyClientHandler(private val nettyClient: NettyClient) : ChannelHandlerAdapter() {
    private val mGson by lazy {
        Gson()
    }

    /**
     * 当客户端连接服务器完成就会触发该方法
     *
     * @param ctx
     * @throws Exception
     */
    override fun channelActive(ctx: ChannelHandlerContext) {
        println("channelActive")
//        val msg = Msg(null, MsgType.ONLINE, null)
//        val buf = Unpooled.copiedBuffer(mGson.toJson(msg).toByteArray(CharsetUtil.UTF_8))
//        ctx.writeAndFlush(buf)
    }

    // channel 处于不活动状态时调用
    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("channelInactive")
        nettyClient.onDisConnected?.invoke()
        nettyClient.reConnect()
    }

    //当通道有读取事件时会触发，即服务端发送数据给客户端
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val str = (msg as ByteBuf).toString(CharsetUtil.UTF_8)
        // 截取有效数据，避免服务端的smart-socket框架添加了一些协议头在数据前面造成解析失败。
        val jsonStr = str.substring(str.indexOf("{"), str.length)
        println("channelRead $jsonStr")
        val m = mGson.fromJson(jsonStr, Msg::class.java)
        when (m.msgType) {
            MsgType.HEART -> {// 心跳
                ctx.writeAndFlush(msg)
            }
            MsgType.ONLINE -> {// 上线消息

            }
            MsgType.OFFLINE -> {// 下线消息

            }
            MsgType.SYS_NOTICE -> {// 系统通知

            }
            MsgType.INSIDE_MSG -> {// 站内信

            }
            MsgType.PHONE -> {// 发起通话

            }
            else -> {}
        }
        nettyClient.onMessageReceived.invoke(m)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("exceptionCaught $cause")
        cause.printStackTrace()
        ctx.close()
    }
}
