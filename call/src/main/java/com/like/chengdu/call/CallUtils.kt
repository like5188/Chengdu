package com.like.chengdu.call

import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


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
            data = Uri.parse("tel:$phoneNumber")
            context.startActivity(this)
        }
    }

    /**
     * 获取[phoneNumber]对应的最近的通话记录
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
     * 获取[num]条最近的通话记录
     */
    @RequiresPermission(allOf = [Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG])
    suspend fun getLatestCalls(context: Context, num: Int): List<Call> =
        withContext(Dispatchers.IO) {
            var i = 0
            val result = mutableListOf<Call>()
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                Call.getProjection(),
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER
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
    val date: Long?,//创建时间点
    val duration: Int?,//通话时长
    var recordingFileUrl: String? = null// 录音文件网络地址
) {

    companion object {
        fun getProjection() = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        fun parse(cursor: Cursor): Call {
            val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            return Call(
                if (nameColumnIndex == -1) null else cursor.getString(nameColumnIndex),
                if (numberColumnIndex == -1) null else cursor.getString(numberColumnIndex),
                if (dateColumnIndex == -1) null else cursor.getLong(dateColumnIndex),
                if (durationColumnIndex == -1) null else cursor.getInt(durationColumnIndex),
            )
        }

    }

    override fun toString(): String {
        return "name=$name, number=$number, date=${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)}, duration=${duration}秒"
    }

}
