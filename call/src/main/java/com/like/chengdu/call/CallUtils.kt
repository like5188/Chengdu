package com.like.chengdu.call

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.provider.CallLog
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * 通话记录工具类
 */
object CallUtils {

    /**
     * 拨打电话（直接拨打电话）
     * @param phoneNumber 电话号码
     */
    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun call(context: Context, phoneNumber: String) {
        Intent(Intent.ACTION_CALL).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("tel:$phoneNumber")
            context.startActivity(this)
        }
    }

    /**
     * 从系统数据库中获取[phoneNumber]对应的最近的通话记录
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG])
    suspend fun getLatestCallByPhoneNumber(context: Context, phoneNumber: String): Call? =
        withContext(Dispatchers.IO) {
            var result: Call? = null
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                Call.getProjection(),
                CallLog.Calls.NUMBER + "=?",
                arrayOf(phoneNumber),
                CallLog.Calls.DEFAULT_SORT_ORDER
            )?.use {
                if (!it.moveToFirst()) {
                    return@use
                }
                result = Call.parse(it)
            }
            result
        }

    /**
     * 从系统数据库中获取[num]条最近的通话记录
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG])
    suspend fun getLatestCalls(context: Context, num: Int): List<Call> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<Call>()
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                Call.getProjection(),
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER
            )?.use {
                while (it.moveToNext() && result.size < num) {
                    result.add(Call.parse(it))
                }
            }
            result
        }

}
