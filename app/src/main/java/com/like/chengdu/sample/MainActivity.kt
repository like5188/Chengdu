package com.like.chengdu.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.*
import com.like.chengdu.socket.client.NettyClient
import com.like.common.util.Logger
import com.like.common.util.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val nettyClient by lazy {
        NettyClient("192.168.31.112", 60000) {
            println("收到服务端的消息:$it")
        }
    }
    private val audioUtils by lazy {
        AudioUtils()
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

    fun phoneState(view: View) {
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
            ).all { it.value }
            if (!requestMultiplePermissions) {
                return@launch
            }
            PhoneReceiver.listen(
                this@MainActivity,
                {
                    Logger.e("接听")
                },
                {
                    Logger.e("挂断")
                    lifecycleScope.launch {
                        // 获取通话记录
                        Logger.d(CallUtils.getLatestCallByPhoneNumber(this@MainActivity, it))
                        // 获取录音文件
                        val config = NetApi.getScanCallRecordingConfig(
                            "xxx",
                            RomUtils.romInfo.name,
                            RomUtils.romInfo.version,
                            Build.VERSION.SDK_INT
                        )
                        Logger.d(CallRecordingUtils.getLastModifiedCallRecordingFile(config))
                    }
                }
            )
        }
    }

    fun call(view: View) {
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.CALL_PHONE,
            ).all { it.value }
            if (!requestMultiplePermissions) {
                return@launch
            }
            CallUtils.call(this@MainActivity, "10000")
        }
    }

    private var i = 0
    fun start(view: View) {
        if (i++ == 0) {
            audioUtils.start("http://www.ytmp3.cn/down/57799.mp3")
        } else {
            audioUtils.start("http://www.ytmp3.cn/down/57790.mp3")
        }
    }

    fun pause(view: View) {
        audioUtils.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtils.destroy()
    }

}
