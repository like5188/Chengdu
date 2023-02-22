package com.like.chengdu.call

import android.content.ContentValues
import android.database.Cursor
import android.provider.CallLog
import java.text.SimpleDateFormat

/**
 * 通话记录
 */
open class Call(
    val id: Int?,
    val name: String?,//联系人
    val number: String?,//被叫号码
    val dateOfCallOccurred: Long?,//开始时间
    val duration: Int?,//通话时长 接通才有，接通后到挂断的时间。秒
) {

    private val sdf by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    override fun toString(): String {
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
                "开始时间=${formatTime(dateOfCallOccurred)},\n" +
                "通话时长=${duration} 秒"
    }

    protected fun formatTime(time: Long?): String {
        if (time == null || time <= 0) {
            return ""
        }
        return try {
            sdf.format(time)
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        internal fun getProjection() = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        internal fun parse(cursor: Cursor): Call {
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

}

class LocalCall(
    id: Int?,
    name: String?,
    number: String?,
    dateOfCallOccurred: Long?,
    duration: Int?
) : Call(id, name, number, dateOfCallOccurred, duration) {
    var dateOfCallHungUp: Long? = null//结束时间
    var recordingFile: String? = null//录音文件本地地址
    var recordingFileUrl: String? = null//录音文件网络地址

    //挂断原因 0: "未知",1: "呼叫失败",2: "我方取消通话",3: "对方挂断",4: "我方挂断",
    val reasonOfHungUp: String?
        get() {
            val duration = this.duration
            return if (duration != null) {
                if (duration > 0) {
                    "未知"
                } else {
                    "呼叫失败"
                }
            } else {
                null
            }
        }

    //持续时间 开始拨打到挂断的时间。秒
    val startToFinishTime: Long?
        get() {
            val dateOfCallOccurred = this.dateOfCallOccurred
            val dateOfCallHungUp = this.dateOfCallHungUp
            return if (dateOfCallOccurred != null && dateOfCallOccurred > 0 && dateOfCallHungUp != null && dateOfCallHungUp > 0) {
                val diff = dateOfCallHungUp - dateOfCallOccurred
                diff / 1000 + if (diff % 1000 == 0L) 0 else 1// 向上取整。
            } else {
                null
            }
        }

    //接通时间
    val dateOfCallConnected: Long?
        get() {
            val duration = this.duration
            val dateOfCallHungUp = this.dateOfCallHungUp
            return if (duration != null && duration > 0 && dateOfCallHungUp != null && dateOfCallHungUp > 0) {
                dateOfCallHungUp - duration * 1000
            } else {
                null
            }
        }

    //呼叫状态 已接通 未接通
    val callState: String?
        get() {
            val duration = this.duration
            return if (duration != null) {
                if (duration > 0) {
                    "已接通"
                } else {
                    "未接通"
                }
            } else {
                null
            }
        }

    constructor(call: Call) : this(
        call.id,
        call.name,
        call.number,
        call.dateOfCallOccurred,
        call.duration
    )

    override fun toString(): String {
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
                "呼叫状态=$callState,\n" +
                "开始时间=${formatTime(dateOfCallOccurred)},\n" +
                "接通时间=${formatTime(dateOfCallConnected)},\n" +
                "结束时间=${formatTime(dateOfCallHungUp)},\n" +
                "通话时长=${duration} 秒,\n" +
                "持续时间=${startToFinishTime} 秒\n" +
                "挂断原因=$reasonOfHungUp,\n" +
                "录音文件本地地址=$recordingFile,\n" +
                "录音文件网络地址=$recordingFileUrl"
    }

    internal fun getContentValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("name", name)
        put("number", number)
        put("dateOfCallOccurred", dateOfCallOccurred)
        put("duration", duration)
        put("dateOfCallHungUp", dateOfCallHungUp)
        put("recordingFile", recordingFile)
        put("recordingFileUrl", recordingFileUrl)
    }

    companion object {
        internal fun getCreateDbSql(): String {
            return "id INTEGER PRIMARY KEY," +
                    "name VARCHAR," +
                    "number VARCHAR," +
                    "dateOfCallOccurred INTEGER," +
                    "duration INTEGER," +
                    "dateOfCallHungUp INTEGER," +
                    "recordingFile VARCHAR," +
                    "recordingFileUrl VARCHAR"
        }

        internal fun parse(cursor: Cursor) = LocalCall(
            cursor.getInt(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getLong(3),
            cursor.getInt(4)
        ).apply {
            dateOfCallHungUp = cursor.getLong(5)
            recordingFile = cursor.getString(6)
            recordingFileUrl = cursor.getString(7)
        }
    }

}
