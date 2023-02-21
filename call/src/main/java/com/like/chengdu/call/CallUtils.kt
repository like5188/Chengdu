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
    val duration: Int?,//通话时长 接通才有，接通后到挂断的时间。
) {
    var recordingFile: String? = null//录音文件本地地址
    var recordingFileUrl: String? = null//录音文件网络地址
    var dateOfCallConnected: Long? = null//接通时间
    var dateOfCallHungUp: Long? = null//结束时间
    var reasonOfHungUp: String? = null//挂断原因 0: "未知",1: "呼叫失败",2: "我方取消通话",3: "对方挂断",4: "我方挂断",
    var callState: String? = null//呼叫状态 已接通 未接通
    var startToFinishTime: Long? = null//持续时间 开始拨打到挂断的时间。

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

    fun localCallToString(): String {
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
                "开始时间=${formatTime(dateOfCallOccurred)},\n" +
                "通话时长=${duration} 秒,\n" +
                "录音文件本地地址=$recordingFile,\n" +
                "录音文件网络地址=$recordingFileUrl,\n" +
                "接通时间=${formatTime(dateOfCallConnected)},\n" +
                "结束时间=${formatTime(dateOfCallHungUp)},\n" +
                "挂断原因=$reasonOfHungUp,\n" +
//                "呼叫状态=$callState,\n" +
                "持续时间=${startToFinishTime} 秒"
    }

    fun systemCallToString(): String {
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
                "开始时间=${formatTime(dateOfCallOccurred)},\n" +
                "通话时长=${duration} 秒"
    }

    private fun formatTime(time: Long?): String {
        if (time == null || time <= 0) {
            return ""
        }
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)
        } catch (e: Exception) {
            ""
        }
    }

}
