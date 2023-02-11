package com.like.chengdu.call

import android.content.Context
import android.provider.CallLog
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object CallHelper {
    fun getCallRecord(context: Context, num: Int) {
        var i = 0
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI, arrayOf(
                CallLog.Calls.CACHED_NAME,  //姓名
                CallLog.Calls.NUMBER,  //号码
                CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
                CallLog.Calls.DATE,  //拨打时间
                CallLog.Calls.DURATION
            ), null, null, CallLog.Calls.DEFAULT_SORT_ORDER
        )?.use {
            if (!it.moveToFirst()) {
                return
            }
            while (!it.isAfterLast && i < num) {
                val callName = it.getString(0) //名称
                val callNumber = it.getString(1) //号码
                //通话类型
                val callType = it.getInt(2)
                if (callType != CallLog.Calls.INCOMING_TYPE &&
                    callType != CallLog.Calls.OUTGOING_TYPE &&
                    callType != CallLog.Calls.MISSED_TYPE
                ) {
                    it.moveToNext()
                    continue
                }
                //拨打时间
                val callDate = it.getLong(3)

                //通话时长
                val callDuration = it.getInt(4)
                Log.i("Msg", "callName:$callName")
                Log.i("Msg", "callNumber:$callNumber")
                Log.i("Msg", "callType:$callType")
                Log.i("Msg", "callDate:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(callDate))}")
                Log.i("Msg", "callDuration:$callDuration")
                it.moveToNext()
                i++
            }
        }
    }

}