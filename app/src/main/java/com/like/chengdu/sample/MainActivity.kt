package com.like.chengdu.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.like.chengdu.socket.client.NettyClient

class MainActivity : AppCompatActivity() {
    private val nettyClient by lazy {
        NettyClient("192.168.31.112", 60000) {
            println("收到服务端的消息:$it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun connect(view: View) {
        nettyClient.connect()
    }

    fun disconnect(view: View) {
        nettyClient.disconnect()
    }

}
