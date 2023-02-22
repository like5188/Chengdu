package com.like.chengdu

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.like.chengdu.call.CallManager
import com.like.chengdu.socket.client.MsgType
import com.like.chengdu.socket.client.NettyClient

class MergeManager(context: Context, host: String?, port: Int?) {
    private val callManager by lazy {
        CallManager(this) { localCall, uploadFile, uploadLocalCall ->
        }
    }
    private val nettyClient by lazy {
        NettyClient(
            onConnected = {
                Toast.makeText(context, "socket 已连接", Toast.LENGTH_SHORT).show()
            },
            onDisConnected = {
                Toast.makeText(context, "socket 未连接", Toast.LENGTH_SHORT).show()
            }
        ) {
            when (it.msgType) {
                MsgType.PHONE -> {// 发起通话

                }
                MsgType.ONLINE -> {// 上线消息

                }
                MsgType.OFFLINE -> {// 下线消息

                }
                MsgType.SYS_NOTICE -> {// 系统通知

                }
                MsgType.INSIDE_MSG -> {// 站内信

                }
                else -> {}
            }
        }
    }

    init {
        nettyClient.connect(host, port)
    }

    fun destroy() {
        nettyClient.disconnect()
    }

}
