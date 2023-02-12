package com.like.chengdu.call

import android.content.Context
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 通话记录工具类
 */
object CallUtils {

    suspend fun getCallRecord(context: Context, num: Int): List<CallRecord> = withContext(Dispatchers.IO) {
        var i = 0
        val result = mutableListOf<CallRecord>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI, CallRecord.getProjection(), null, null, CallLog.Calls.DEFAULT_SORT_ORDER
        )?.use {
            if (!it.moveToFirst()) {
                return@use
            }
            while (!it.isAfterLast && i < num) {
                result.add(CallRecord.parse(it))
                it.moveToNext()
                i++
            }
        }
        result
    }

}