package com.like.chengdu.sample

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.CallHelper
import com.like.chengdu.socket.client.NettyClient
import com.like.common.util.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.launch

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

    fun getCallRecord(view: View) {
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
            ).all { it.value }
            if (!requestMultiplePermissions) {
                return@launch
            }
            CallHelper.getCallRecord(this@MainActivity, 10)
        }
    }

}
