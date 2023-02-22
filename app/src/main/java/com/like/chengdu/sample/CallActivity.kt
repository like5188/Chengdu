package com.like.chengdu.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.AudioUtils
import com.like.chengdu.call.CallManager
import com.like.chengdu.call.CallUtils
import com.like.chengdu.call.LocalCall
import com.like.chengdu.sample.databinding.ActivityCallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class CallActivity : AppCompatActivity() {
    private val mBinding: ActivityCallBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_call)
    }
    private val audioUtils by lazy {
        AudioUtils()
    }
    private var curLocalCall: LocalCall? = null
    private val callManager by lazy {
        CallManager(this) { localCall, uploadFile, uploadLocalCall ->
            curLocalCall = localCall
            // 更新ui
            mBinding.tvCall.text = localCall.toString()
            if (uploadFile) {
                mBinding.tvUploadFile.text = "上传录音文件成功!"
                mBinding.tvUploadFile.setTextColor(Color.parseColor("#00ff00"))
            } else {
                mBinding.tvUploadFile.text = "上传录音文件失败!"
                mBinding.tvUploadFile.setTextColor(Color.parseColor("#ff0000"))
            }
            if (uploadLocalCall) {
                mBinding.tvUploadCall.text = "上传通话记录成功!"
                mBinding.tvUploadCall.setTextColor(Color.parseColor("#00ff00"))
            } else {
                mBinding.tvUploadCall.text = "上传通话记录失败!"
                mBinding.tvUploadCall.setTextColor(Color.parseColor("#ff0000"))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.etMsg.movementMethod = ScrollingMovementMethod.getInstance()
        callManager
    }

    fun call(view: View) {
        val phone = mBinding.etPhone.text?.toString()
        if (phone.isNullOrEmpty()) {
            return
        }
        curLocalCall = null
        mBinding.tvCall.text = ""
        mBinding.tvUploadCall.text = ""
        mBinding.tvUploadFile.text = ""
        lifecycleScope.launch {
            callManager.call(phone, MyApplication.config)
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
