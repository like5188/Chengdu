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
import com.like.chengdu.sample.databinding.ActivityCallBinding
import com.like.common.util.Logger
import com.like.common.util.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("MissingPermission")
class CallActivity : AppCompatActivity() {
    private val mBinding: ActivityCallBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_call)
    }
    private val audioUtils by lazy {
        AudioUtils()
    }
    private var config: ScanCallRecordingConfig? = null
    private val callRecordingFileUtils by lazy {
        CallRecordingFileUtils()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.etMsg.movementMethod = ScrollingMovementMethod.getInstance()
        lifecycleScope.launch {
            config = NetApi.getScanCallRecordingConfig("http://47.108.214.93/call.json")?.apply {
                callRecordingFileUtils.init(this)
            }
        }
        listenPhoneState()
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
                this@CallActivity,
                {
                    Logger.e("接听")
                    dateOfCallConnected = System.currentTimeMillis()
                },
                {
                    Logger.e("挂断")
                    val hungUpTime = System.currentTimeMillis()
                    lifecycleScope.launch(Dispatchers.IO) {
                        listenOnceCallLogChange {
                            // 获取通话记录
                            val call = CallUtils.getLatestCallByPhoneNumber(this@CallActivity, it)?.apply {
                                this.dateOfCallConnected = dateOfCallConnected
                                this.dateOfCallHungUp = hungUpTime
                                this.dateOfCallOccurred?.let {
                                    this.startToFinishTime = (hungUpTime - it) / 1000
                                }
                                dateOfCallConnected = null
                            }
                            withContext(Dispatchers.Main) {
                                mBinding.tvCall.text = call?.toString() ?: ""
                            }
                            if (call == null) return@listenOnceCallLogChange

                            // 获取录音文件
                            val file = callRecordingFileUtils.getCallRecordingFile()
                            withContext(Dispatchers.Main) {
                                mBinding.tvCallRecordingFile.text = file?.absolutePath ?: ""
                            }

                            val uploadResult = UploadUtils.upload(this@CallActivity, call, file)
                            withContext(Dispatchers.Main) {
                                updateCallRecordingFileTextColor(uploadResult.first)
                                updateCallTextColor(uploadResult.second)
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * 监听一次系统通话记录数据库的改变
     */
    private suspend fun listenOnceCallLogChange(onChanged: suspend () -> Unit) {
        val stateFlow = MutableStateFlow(false)
        val callLogObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                stateFlow.value = selfChange
            }
        }
        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            callLogObserver
        )
        stateFlow.debounce(500).collectLatest {
            contentResolver.unregisterContentObserver(callLogObserver)
            onChanged.invoke()
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
            withContext(Dispatchers.Main) {
                mBinding.tvCall.text = ""
                mBinding.tvCallRecordingFile.text = ""
            }
            callRecordingFileUtils.start()
            CallUtils.call(this@CallActivity, phone)
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

    fun getCalls(view: View) {
        mBinding.etMsg.setText("")
        lifecycleScope.launch(Dispatchers.Main) {
            CallUtils.getLatestCalls(this@CallActivity, 10).forEach {
                val oldMsg = mBinding.etMsg.text?.toString()
                val text = if (oldMsg.isNullOrEmpty()) {
                    it.toString()
                } else {
                    oldMsg + "\n\n" + it.toString()
                }
                mBinding.etMsg.setText(text)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioUtils.destroy()
        callRecordingFileUtils.destroy()
    }

}
