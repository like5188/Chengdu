package com.like.chengdu.sample

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
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
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val nettyClient by lazy {
        fun updateSocketMsg(newMsg: String) {
            lifecycleScope.launch(Dispatchers.Main) {
                val oldMsg = mBinding.etMsg.text?.toString()
                val text = if (oldMsg.isNullOrEmpty()) {
                    newMsg
                } else {
                    oldMsg + "\n" + newMsg
                }
                mBinding.etMsg.setText(text)
                mBinding.etMsg.setSelection(text.length, text.length)
            }
        }
        NettyClient(
            onConnected = {
                updateSocketMsg("已连接!")
            },
            onDisConnected = {
                updateSocketMsg("!!!未连接!!!")
            }
        ) {
            updateSocketMsg(it)
        }
    }
    private val audioUtils by lazy {
        AudioUtils()
    }
    private var config: ScanCallRecordingConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.etMsg.movementMethod = ScrollingMovementMethod.getInstance()
        listenPhoneState()
        lifecycleScope.launch {
            config = NetApi.getScanCallRecordingConfig("http://47.108.214.93/call.json")
        }
    }

    private fun listenPhoneState() {
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
            var dateOfCallConnected: Long? = null
            PhoneReceiver.listen(
                this@MainActivity,
                {
                    Logger.e("接听")
                    dateOfCallConnected = System.currentTimeMillis()
                },
                {
                    Logger.e("挂断")
                    lifecycleScope.launch(Dispatchers.Main) {
                        // 获取录音文件并上传
                        val url = getAndUploadCallRecordingFile()
                        // 获取通话记录并上传
                        val dateOfCallHungUp = System.currentTimeMillis()
                        getAndUploadCall(it, url, dateOfCallConnected, dateOfCallHungUp)
                        dateOfCallConnected = null
                    }
                }
            )
        }
    }

    private suspend fun getAndUploadCallRecordingFile(): String? = withContext(Dispatchers.Main) {
        val file = CallRecordingUtils.getLastModifiedCallRecordingFile(this@MainActivity, config)
        mBinding.tvCallRecordingFile.text = file?.absolutePath ?: ""
        val url = NetApi.uploadFile("", file)
        if (url != null) {
            mBinding.tvCallRecordingFile.setTextColor(Color.parseColor("#00ff00"))
        } else {
            mBinding.tvCallRecordingFile.setTextColor(Color.parseColor("#ff0000"))
        }
        url
    }

    private suspend fun getAndUploadCall(
        phoneNumber: String,
        url: String?,
        dateOfCallConnected: Long?,
        dateOfCallHungUp: Long?
    ) = withContext(Dispatchers.Main) {
        val call = CallUtils.getLatestCallByPhoneNumber(this@MainActivity, phoneNumber)?.apply {
            this.recordingFileUrl = url
            this.dateOfCallConnected = dateOfCallConnected
            this.dateOfCallHungUp = dateOfCallHungUp
        }
        mBinding.tvCall.text = call?.toString() ?: ""
        val result = NetApi.uploadCall("", call)
        if (result) {
            mBinding.tvCall.setTextColor(Color.parseColor("#00ff00"))
        } else {
            mBinding.tvCall.setTextColor(Color.parseColor("#ff0000"))
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
        mBinding.etMsg.setText("")
    }

    fun call(view: View) {
        val phone = mBinding.etPhone.text?.toString()
        if (phone.isNullOrEmpty()) {
            return
        }
        lifecycleScope.launch {
            val requestMultiplePermissions = requestMultiplePermissions(
                Manifest.permission.CALL_PHONE,
            ).all { it.value }
            if (!requestMultiplePermissions) {
                return@launch
            }
            CallUtils.call(this@MainActivity, phone)
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
