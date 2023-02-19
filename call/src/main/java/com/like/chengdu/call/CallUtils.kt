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
    val id: Int?,
    val name: String?,//联系人
    val number: String?,//被叫号码
    val dateOfCallOccurred: Long?,//开始时间
    val duration: Int?,//通话时长
) {
    var recordingFileUrl: String? = null//录音文件网络地址
    var dateOfCallConnected: Long? = null//接通时间
    var dateOfCallHungUp: Long? = null//结束时间
    var reasonOfHungUp: String? = null//挂断原因
    var callState: String? = null//呼叫状态
    val xxx1: Int? = null//持续时间

    companion object {
        fun getProjection() = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        fun parse(cursor: Cursor): Call {
            val idColumnIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            return Call(
                if (idColumnIndex == -1) null else cursor.getInt(idColumnIndex),
                if (nameColumnIndex == -1) null else cursor.getString(nameColumnIndex),
                if (numberColumnIndex == -1) null else cursor.getString(numberColumnIndex),
                if (dateColumnIndex == -1) null else cursor.getLong(dateColumnIndex),
                if (durationColumnIndex == -1) null else cursor.getInt(durationColumnIndex),
            )
        }

    }

    override fun toString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
//                "呼叫状态=$callState,\n" +
                "开始时间=${sdf.format(dateOfCallOccurred)},\n" +
//                "接通时间=${sdf.format(dateOfCallConnected)},\n" +
//                "结束时间=${sdf.format(dateOfCallHungUp)},\n" +
                "通话时长=${duration}秒,\n" +
                "持续时间=${xxx1}秒,\n" +
                "挂断原因=$reasonOfHungUp,\n" +
                "录音文件=$recordingFileUrl"
    }

}
