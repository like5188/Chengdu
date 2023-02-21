package com.like.chengdu.sample

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.call.DBHelper
import com.like.chengdu.call.UploadUtils
import com.like.chengdu.sample.databinding.ActivityUploadFailedCallsBinding
import kotlinx.coroutines.launch

class UploadFailedCallsActivity : AppCompatActivity() {
    private val mBinding: ActivityUploadFailedCallsBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_upload_failed_calls)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.etMsg.movementMethod = ScrollingMovementMethod.getInstance()
    }

    fun getUploadFailedCalls(view: View) {
        mBinding.etMsg.setText("")
        lifecycleScope.launch {
            DBHelper.getInstance(this@UploadFailedCallsActivity)?.getCalls()?.forEach {
                val oldMsg = mBinding.etMsg.text?.toString()
                val text = if (oldMsg.isNullOrEmpty()) {
                    it.localCallToString()
                } else {
                    oldMsg + "\n\n" + it.localCallToString()
                }
                mBinding.etMsg.setText(text)
            }
        }
    }

    fun upload(view: View) {
        lifecycleScope.launch {
            UploadUtils.reUploadFail(this@UploadFailedCallsActivity)
        }
    }

}
