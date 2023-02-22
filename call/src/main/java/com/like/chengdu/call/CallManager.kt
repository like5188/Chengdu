package com.like.chengdu.call

import android.Manifest
import android.annotation.SuppressLint
import android.database.ContentObserver
import android.provider.CallLog
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 打电话，查询通话记录和录音文件并上传
 */
@SuppressLint("MissingPermission")
class CallManager(
    private val activity: ComponentActivity,
    private val uploadResult: (LocalCall, uploadFile: Boolean, uploadLocalCall: Boolean) -> Unit
) {
    private val callRecordingFileUtils by lazy {
        CallRecordingFileUtils()
    }

    init {
        activity.lifecycleScope.launch {
            val requestMultiplePermissions = activity.requestMultiplePermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE,
            ).all { it.value }
            if (!requestMultiplePermissions) {
                return@launch
            }
            NetApi.getScanCallRecordingConfig("http://47.108.214.93/call.json")?.apply {
                callRecordingFileUtils.init(this)
            }
            listenPhoneState()
        }
    }

    private suspend fun listenPhoneState() {
        PhoneReceiver.listen(
            activity,
            {
                println("开始嘟嘟嘟……")
            },
            {
                println("挂断")
                val hungUpTime = System.currentTimeMillis()
                activity.lifecycleScope.launch(Dispatchers.IO) {
                    listenOnceCallLogChange {
                        // 获取通话记录
                        val call = CallUtils.getLatestCallByPhoneNumber(activity, it)
                            ?: return@listenOnceCallLogChange
                        val localCall = LocalCall(call).apply {
                            this.dateOfCallHungUp = hungUpTime
                        }

                        // 获取录音文件
                        val files = callRecordingFileUtils.getCallRecordingFile()
                        // 转换成wav格式
                        val file = AudioConverter.convertToWav(files.firstOrNull())
                        // 上传
                        val uploadResult = UploadUtils.upload(activity, localCall, file)
                        withContext(Dispatchers.Main) {
                            uploadResult(localCall, uploadResult.first, uploadResult.second)
                        }
                    }
                }
            }
        )
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
        activity.contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            callLogObserver
        )
        stateFlow.debounce(500).collectLatest {
            activity.contentResolver.unregisterContentObserver(callLogObserver)
            onChanged.invoke()
        }
    }

    fun call(phone: String?) {
        if (phone.isNullOrEmpty()) {
            return
        }
        activity.lifecycleScope.launch {
            callRecordingFileUtils.startWatching()
            CallUtils.call(activity, phone)
        }
    }

}