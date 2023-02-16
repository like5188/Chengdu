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
import java.util.concurrent.atomic.AtomicBoolean

class PhoneReceiver(
    private val onOffHook: (String) -> Unit,
    private val onIdle: (String) -> Unit
) : BroadcastReceiver() {
    private var curPhoneNumber = ""
    private var isOffHooked = AtomicBoolean(false)
    private val myPhoneListener by lazy {
        MyPhoneListener()
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 去电
        if (intent.action === Intent.ACTION_NEW_OUTGOING_CALL) {
            // 获取拨打的号码
            curPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            Log.d("TAG", "onReceive ACTION_NEW_OUTGOING_CALL $$curPhoneNumber")
            // 监听电话状态
            val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
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
            if (curPhoneNumber.isEmpty()) {
                return
            }
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.d(
                        "TAG",
                        "onCallStateChanged CALL_STATE_IDLE curPhoneNumber=$curPhoneNumber"
                    )
                    if (isOffHooked.compareAndSet(true, false)) {
                        onIdle.invoke(curPhoneNumber)// 挂断
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.d(
                        "TAG",
                        "onCallStateChanged CALL_STATE_OFFHOOK curPhoneNumber=$curPhoneNumber"
                    )
                    if (isOffHooked.compareAndSet(false, true)) {
                        onOffHook.invoke(curPhoneNumber)// 接听
                    }
                }
            }
        }

    }

}
