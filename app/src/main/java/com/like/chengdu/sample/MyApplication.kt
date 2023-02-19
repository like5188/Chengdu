package com.like.chengdu.sample

import android.app.Application
import com.like.chengdu.call.UploadUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            UploadUtils.reUploadFail(this@MyApplication)
        }
    }
}