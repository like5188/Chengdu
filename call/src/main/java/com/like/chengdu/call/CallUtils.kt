package com.like.chengdu.call

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 通话记录工具类
 */
object CallUtils {

    suspend fun getCallRecord(context: Context, num: Int): List<Call> = withContext(Dispatchers.IO) {
        var i = 0
        val result = mutableListOf<Call>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI, Call.getProjection(), null, null, CallLog.Calls.DEFAULT_SORT_ORDER
        )?.use {
            if (!it.moveToFirst()) {
                return@use
            }
            while (!it.isAfterLast && i < num) {
                result.add(Call.parse(it))
                it.moveToNext()
                i++
            }
        }
        result
    }

}

/**
 * 通话记录
 */
data class Call(
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

        fun parse(cursor: Cursor): Call {
            val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val typeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            return Call(
                if (nameColumnIndex == -1) null else cursor.getString(nameColumnIndex),
                if (numberColumnIndex == -1) null else cursor.getString(numberColumnIndex),
                if (typeColumnIndex == -1) null else cursor.getInt(typeColumnIndex),
                if (dateColumnIndex == -1) null else cursor.getLong(dateColumnIndex),
                if (durationColumnIndex == -1) null else cursor.getInt(durationColumnIndex),
            )
        }

    }

}
