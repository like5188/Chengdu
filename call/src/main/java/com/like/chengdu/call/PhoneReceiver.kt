package com.like.chengdu.call

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission

class PhoneReceiver(
    private val onOffHook: (String) -> Unit,
    private val onIdle: (String) -> Unit
) : BroadcastReceiver() {
    private var curPhoneNumber = ""
    private val myPhoneListener by lazy {
        MyPhoneListener()
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 如果是去电
        if (intent.action === Intent.ACTION_NEW_OUTGOING_CALL) {
            curPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            Log.d("TAG", "onReceive 呼叫:$$curPhoneNumber")
            val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            // 监听电话状态
            tm.listen(myPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    companion object {
        @RequiresPermission(allOf = [Manifest.permission.READ_PHONE_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS])
        fun listen(context: Context, onOffHook: (String) -> Unit, onIdle: (String) -> Unit) {
            IntentFilter().apply {
                //设置拨号广播过滤
                addAction("android.intent.action.NEW_OUTGOING_CALL")
                addAction("android.intent.action.PHONE_STATE")
                context.registerReceiver(PhoneReceiver(onOffHook, onIdle), this)
            }
        }
    }

    inner class MyPhoneListener : PhoneStateListener() {

        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.d(
                        "TAG",
                        "onCallStateChanged CALL_STATE_IDLE curPhoneNumber=$curPhoneNumber"
                    )
                    phoneNumber?.let {
                        onIdle.invoke(it)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.d(
                        "TAG",
                        "onCallStateChanged CALL_STATE_OFFHOOK curPhoneNumber=$curPhoneNumber"
                    )
                    phoneNumber?.let {
                        onOffHook.invoke(it)
                    }
                }
            }
        }

    }

}
