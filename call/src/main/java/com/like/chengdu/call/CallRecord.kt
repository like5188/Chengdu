package com.like.chengdu.call

import android.database.Cursor
import android.provider.CallLog

data class CallRecord(
    val name: String?,
    val number: String?,//号码
    val type: Int?,//通话类型 CallLog.Calls.INCOMING_TYPE - INCOMING_TYPE.ANSWERED_EXTERNALLY_TYPE
    val date: Long?,//创建时间点
    val duration: Int?,//通话时长
) {
    companion object {
        fun getProjection() = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        fun parse(cursor: Cursor): CallRecord {
            val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val typeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            return CallRecord(
                if (nameColumnIndex == -1) null else cursor.getString(nameColumnIndex),
                if (numberColumnIndex == -1) null else cursor.getString(numberColumnIndex),
                if (typeColumnIndex == -1) null else cursor.getInt(typeColumnIndex),
                if (dateColumnIndex == -1) null else cursor.getLong(dateColumnIndex),
                if (durationColumnIndex == -1) null else cursor.getInt(durationColumnIndex),
            )
        }

    }

}
