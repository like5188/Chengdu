package com.like.chengdu

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.like.chengdu.call.*
import com.like.chengdu.socket.client.MsgType
import com.like.chengdu.socket.client.NettyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MergeManager(context: Context, lifecycleScope: CoroutineScope, host: String?, port: Int?) {
    private var config: ScanCallRecordingConfig = ScanCallRecordingConfig()
    private val callManager by lazy {
        CallManager(context, lifecycleScope)
    }
    private val audioUtils by lazy {
        AudioUtils()
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
                    lifecycleScope.launch {
                        callManager.call(it.msgContent, config)
                    }
                }
                MsgType.ONLINE, MsgType.OFFLINE, MsgType.SYS_NOTICE, MsgType.INSIDE_MSG -> {// 上线消息,下线消息,系统通知,站内信
                    // todo 转发H5
                    println("收到消息：$it")
                }
                else -> {}
            }
        }
    }

    init {
        nettyClient.connect(host, port)
        lifecycleScope.launch {
            NetApi.getScanCallRecordingConfig("http://47.108.214.93/call.json")?.apply {
                config = this
            }
        }
        lifecycleScope.launch {
            UploadUtils.reUploadFail(context)
        }
    }

    fun play(url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        audioUtils.start(url)
    }

    fun pause() {
        audioUtils.pause()
    }

    fun destroy() {
        nettyClient.disconnect()
        audioUtils.destroy()
    }

}
