package com.like.chengdu.sample

import android.app.Application
import com.like.chengdu.call.NetApi
import com.like.chengdu.call.ScanCallRecordingConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {
    companion object {
        var config: ScanCallRecordingConfig = ScanCallRecordingConfig()
    }

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            NetApi.getScanCallRecordingConfig("http://47.108.214.93/call.json")?.apply {
                config = this
            }
        }
    }
}