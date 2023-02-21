package com.like.chengdu.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.chengdu.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
    }

    fun testCall(view: View) {
        startActivity(Intent(this, CallActivity::class.java))
    }

    fun testSocket(view: View) {
        startActivity(Intent(this, SocketActivity::class.java))
    }

    fun uploadFailedCalls(view: View) {
        startActivity(Intent(this, UploadFailedCallsActivity::class.java))
    }

}
