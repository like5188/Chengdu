package com.like.chengdu.sample

import android.Manifest
import android.annotation.SuppressLint
import android.database.ContentObserver
import android.graphics.Color
import android.os.Bundle
import android.provider.CallLog
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
                    val hungUpTime = System.currentTimeMillis()
                    val callLogObserver = object : ContentObserver(null) {

                        override fun onChange(selfChange: Boolean) {
                            super.onChange(selfChange)
                            contentResolver.unregisterContentObserver(this)
                            lifecycleScope.launch(Dispatchers.IO) {
                                // 获取通话记录
                                val call =
                                    CallUtils.getLatestCallByPhoneNumber(this@MainActivity, it)
                                        ?.apply {
                                            this.dateOfCallConnected = dateOfCallConnected
                                            this.dateOfCallHungUp = hungUpTime
                                            this.dateOfCallOccurred?.let {
                                                this.startToFinishTime = hungUpTime - it
                                            }
                                            dateOfCallConnected = null
                                        }
                                withContext(Dispatchers.Main) {
                                    mBinding.tvCall.text = call?.toString() ?: ""
                                }
                                if (call == null) return@launch

                                // 获取录音文件
                                val file = CallRecordingUtils.getLastModifiedCallRecordingFile(
                                    this@MainActivity,
                                    config
                                )
                                withContext(Dispatchers.Main) {
                                    mBinding.tvCallRecordingFile.text = file?.absolutePath ?: ""
                                }

                                val uploadResult = UploadUtils.upload(this@MainActivity, call, file)
                                withContext(Dispatchers.Main) {
                                    updateCallRecordingFileTextColor(uploadResult.first)
                                    updateCallTextColor(uploadResult.second)
                                }
                            }
                        }
                    }
                    contentResolver.registerContentObserver(
                        CallLog.Calls.CONTENT_URI,
                        true,
                        callLogObserver
                    )
                }
            )
        }
    }

    private fun updateCallRecordingFileTextColor(uploadSuccess: Boolean) {
        if (uploadSuccess) {// 成功
            mBinding.tvCallRecordingFile.setTextColor(Color.parseColor("#00ff00"))
        } else {// 失败
            mBinding.tvCallRecordingFile.setTextColor(Color.parseColor("#ff0000"))
        }
    }

    private fun updateCallTextColor(uploadSuccess: Boolean) {
        if (uploadSuccess) {// 成功
            mBinding.tvCall.setTextColor(Color.parseColor("#00ff00"))
        } else {// 失败
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
