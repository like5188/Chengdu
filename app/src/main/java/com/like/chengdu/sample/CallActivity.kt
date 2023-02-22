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
    private var curLocalCall: LocalCall? = null

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

            PhoneReceiver.listen(
                this@CallActivity,
                {
                    Logger.e("开始嘟嘟嘟……")
                },
                {
                    Logger.e("挂断")
                    val hungUpTime = System.currentTimeMillis()
                    lifecycleScope.launch(Dispatchers.IO) {
                        listenOnceCallLogChange {
                            // 获取通话记录
                            val call = CallUtils.getLatestCallByPhoneNumber(this@CallActivity, it)
                                ?: return@listenOnceCallLogChange
                            val localCall = LocalCall(call).apply {
                                this.dateOfCallHungUp = hungUpTime
                                this.dateOfCallOccurred?.let {
                                    this.startToFinishTime = (hungUpTime - it) / 1000
                                }
                            }

                            // 获取录音文件
                            val files = callRecordingFileUtils.getCallRecordingFile()
                            // 转换成wav格式
                            val file = AudioConverter.convertToWav(files.firstOrNull())
                            // 上传文件
                            val uploadResult =
                                UploadUtils.upload(this@CallActivity, localCall, file)
                            // 更新ui
                            withContext(Dispatchers.Main) {
                                mBinding.tvCall.text = localCall.toString()
                                if (uploadResult.first) {
                                    mBinding.tvUploadFile.text = "上传录音文件成功!"
                                    mBinding.tvUploadFile.setTextColor(Color.parseColor("#00ff00"))
                                } else {
                                    mBinding.tvUploadFile.text = "上传录音文件失败!"
                                    mBinding.tvUploadFile.setTextColor(Color.parseColor("#ff0000"))
                                }
                                if (uploadResult.second) {
                                    mBinding.tvUploadCall.text = "上传通话记录成功!"
                                    mBinding.tvUploadCall.setTextColor(Color.parseColor("#00ff00"))
                                } else {
                                    mBinding.tvUploadCall.text = "上传通话记录失败!"
                                    mBinding.tvUploadCall.setTextColor(Color.parseColor("#ff0000"))
                                }
                            }
                            curLocalCall = localCall
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
            curLocalCall = null
            withContext(Dispatchers.Main) {
                mBinding.tvCall.text = ""
                mBinding.tvUploadCall.text = ""
                mBinding.tvUploadFile.text = ""
            }
            callRecordingFileUtils.startWatching()
            CallUtils.call(this@CallActivity, phone)
        }
    }

    fun start(view: View) {
        // 上传成功后就播放网络地址，否则播放本地地址
        val recordingFileUrl = curLocalCall?.recordingFileUrl
        val recordingFile = curLocalCall?.recordingFile
        val filePath = if (!recordingFileUrl.isNullOrEmpty()) {
            recordingFileUrl
        } else {
            recordingFile
        }
        if (filePath.isNullOrEmpty()) {
            return
        }
        audioUtils.start(filePath)
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
    }

}
