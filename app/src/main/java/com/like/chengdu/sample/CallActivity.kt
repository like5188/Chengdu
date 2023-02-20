package com.like.chengdu.sample

import android.Manifest
import android.annotation.SuppressLint
import android.database.ContentObserver
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.provider.CallLog
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.*
import com.like.chengdu.sample.databinding.ActivityCallBinding
import com.like.common.util.Logger
import com.like.common.util.activityresultlauncher.requestMultiplePermissions
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("MissingPermission")
class CallActivity : AppCompatActivity() {
    private val mBinding: ActivityCallBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_call)
    }
    private val audioUtils by lazy {
        AudioUtils()
    }
    private var config: ScanCallRecordingConfig? = null
//    private val fileObserver by lazy {
//        object : FileObserver(
//            File(
//                Environment.getExternalStorageDirectory(),
//                "/Music/Recordings/Call Recordings"
//            )
//        ) {
//            override fun onEvent(event: Int, path: String?) {
//                val action = event and ALL_EVENTS
//                println("path:$path action:$action")
////                when (action) {
////                    ACCESS -> println("event: 文件或目录被访问, path: $path")
////                    DELETE -> println("event: 文件或目录被删除, path: $path")
////                    OPEN -> println("event: 文件或目录被打开, path: $path")
////                    MODIFY -> println("event: 文件或目录被修改, path: $path")
////                    CREATE -> println("event: 文件或目录被创建, path: $path")
////                }
////                this.stopWatching()
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
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
//            fileObserver.startWatching()

            var dateOfCallConnected: Long? = null
            PhoneReceiver.listen(
                this@CallActivity,
                {
                    Logger.e("接听")
                    dateOfCallConnected = System.currentTimeMillis()
                },
                {
                    Logger.e("挂断")
//                    val hungUpTime = System.currentTimeMillis()
//                    listenOnceCallLogChange {
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            // 获取通话记录
//                            val call =
//                                CallUtils.getLatestCallByPhoneNumber(this@MainActivity, it)?.apply {
//                                    this.dateOfCallConnected = dateOfCallConnected
//                                    this.dateOfCallHungUp = hungUpTime
//                                    this.dateOfCallOccurred?.let {
//                                        this.startToFinishTime = hungUpTime - it
//                                    }
//                                    dateOfCallConnected = null
//                                }
//                            withContext(Dispatchers.Main) {
//                                mBinding.tvCall.text = call?.toString() ?: ""
//                            }
//                            if (call == null) return@launch
//
//                            // 获取录音文件
//                            val file = CallRecordingUtils.getLastModifiedCallRecordingFile(
//                                this@MainActivity,
//                                config
//                            )
//                            withContext(Dispatchers.Main) {
//                                mBinding.tvCallRecordingFile.text = file?.absolutePath ?: ""
//                            }
//
//                            val uploadResult = UploadUtils.upload(this@MainActivity, call, file)
//                            withContext(Dispatchers.Main) {
//                                updateCallRecordingFileTextColor(uploadResult.first)
//                                updateCallTextColor(uploadResult.second)
//                            }
//                        }
//                    }
                }
            )
        }
    }

    private fun listenCallRecordingFile() {

    }

    private fun listenOnceCallRecordingDirChange(onChanged: () -> Unit) {
        val parent = Environment.getExternalStorageDirectory()
        val fileObservers = mutableListOf<FileObserver>()
        config?.getFilePaths()?.forEach { filePath ->
            val dir = File(parent, filePath)
            if (dir.exists() && dir.isDirectory) {
                fileObservers.add(
                    object : FileObserver(dir) {
                        override fun onEvent(event: Int, path: String?) {
                            println("event: $event, path: $path")
                            when (event and ALL_EVENTS) {
                                ACCESS -> println("event: 文件或目录被访问, path: $path")
                                DELETE -> println("event: 文件或目录被删除, path: $path")
                                OPEN -> println("event: 文件或目录被打开, path: $path")
                                MODIFY -> println("event: 文件或目录被修改, path: $path")
                                CREATE -> println("event: 文件或目录被创建, path: $path")
                            }
                            this.stopWatching()
                        }
                    }
                )
            }
        }
        fileObservers.forEach {
            it.startWatching()
        }
    }

    /**
     * 监听一次系统通话记录数据库的改变
     */
    private fun listenOnceCallLogChange(onChanged: () -> Unit) {
        val callLogObserver = object : ContentObserver(null) {

            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                contentResolver.unregisterContentObserver(this)
                onChanged.invoke()
            }
        }
        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            callLogObserver
        )
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

    override fun onDestroy() {
        super.onDestroy()
        audioUtils.destroy()
    }

}
