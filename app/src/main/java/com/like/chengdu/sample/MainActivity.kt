package com.like.chengdu.sample

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.*
import com.like.chengdu.sample.databinding.ActivityMainBinding
import com.like.chengdu.socket.client.NettyClient
import com.like.common.util.Logger
import com.like.common.util.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val nettyClient by lazy {
        NettyClient {
            lifecycleScope.launch(Dispatchers.Main) {
                val oldMsg = mBinding.tvMsg.text?.toString()
                mBinding.tvMsg.text = if (oldMsg.isNullOrEmpty()) {
                    it
                } else {
                    oldMsg + "\n" + it
                }
            }
        }
    }
    private val audioUtils by lazy {
        AudioUtils()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
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
                        mBinding.tvCall.text = CallUtils.getLatestCallByPhoneNumber(this@MainActivity, it).toString()
                        // 获取录音文件
                        val config = NetApi.getScanCallRecordingConfig(
                            "xxx",
                            RomUtils.romInfo.name,
                            RomUtils.romInfo.version,
                            Build.VERSION.SDK_INT
                        )
                        mBinding.tvCallRecordingFile.text =
                            CallRecordingUtils.getLastModifiedCallRecordingFile(config)?.absolutePath ?: ""
                    }
                }
            )
        }
    }

    fun connect(view: View) {
        val host = mBinding.etHost.text?.toString() ?: ""
        val port = mBinding.etPort.text?.toString()?.toInt() ?: -1
        if (host.isEmpty() || port == -1) {
            return
        }
        nettyClient.connect(host, port)
    }

    fun disconnect(view: View) {
        nettyClient.disconnect()
    }

    fun clearMsg(view: View) {
        mBinding.tvMsg.text = ""
    }

    fun listenPhoneState(view: View) {
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
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
                        mBinding.tvCall.text = CallUtils.getLatestCallByPhoneNumber(this@MainActivity, it).toString()
                        // 获取录音文件
                        val config = NetApi.getScanCallRecordingConfig(
                            "xxx",
                            RomUtils.romInfo.name,
                            RomUtils.romInfo.version,
                            Build.VERSION.SDK_INT
                        )
                        mBinding.tvCallRecordingFile.text =
                            CallRecordingUtils.getLastModifiedCallRecordingFile(config)?.absolutePath ?: ""
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

    //    private var i = 0
    fun start(view: View) {
        val filePath = mBinding.tvCallRecordingFile.text?.toString()
        if (filePath.isNullOrEmpty()) {
            return
        }
        audioUtils.start(filePath)
//        if (i++ == 0) {
//            audioUtils.start("http://www.ytmp3.cn/down/57799.mp3")
//        } else {
//            audioUtils.start("http://www.ytmp3.cn/down/57790.mp3")
//        }
    }

    fun pause(view: View) {
        audioUtils.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtils.destroy()
    }

}
