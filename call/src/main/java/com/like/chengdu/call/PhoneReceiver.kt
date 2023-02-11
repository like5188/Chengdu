package com.like.chengdu.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

class PhoneReceiver : BroadcastReceiver() {
    private var curPhoneNumber = ""

    override fun onReceive(context: Context, intent: Intent) {
        // 如果是去电
        if (intent.action === Intent.ACTION_NEW_OUTGOING_CALL) {
            curPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) ?: ""
            Toast.makeText(context.applicationContext, "呼叫:$curPhoneNumber", Toast.LENGTH_LONG).show()
        } else {
            val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            // 监听电话状态
            tm.listen(MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    companion object {
        fun listen(context: Context) {
            val phoneReceiver = PhoneReceiver()
            val intentFilter = IntentFilter()
            //设置拨号广播过滤
            intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL")
            intentFilter.addAction("android.intent.action.PHONE_STATE")
            context.registerReceiver(phoneReceiver, intentFilter)
        }
    }

    inner class MyPhoneListener : PhoneStateListener() {

        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            Log.w("TAG", "onCallStateChanged state:$state phoneNumber:$phoneNumber curPhoneNumber=$curPhoneNumber")
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.e("TAG", "空闲")
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.e("TAG", "接听")
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    Log.e("TAG", "响铃")
                }
            }
        }

    }

}
