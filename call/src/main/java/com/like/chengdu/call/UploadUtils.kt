package com.like.chengdu.call

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object UploadUtils {

    /**
     * 上传通话记录和录音文件
     *
     * @return first:录音文件是否上传成功;second:通话记录是否上传成功
     */
    suspend fun upload(context: Context, call: Call, file: File?): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        var uploadFileSuccess = false
        if (file != null) {
            call.recordingFile = file.absolutePath
            // 上传录音文件
            val url = NetApi.uploadFile("", file)
            uploadFileSuccess = url != null
            if (url != null) {// 成功
                call.recordingFileUrl = url
            }
        }
        // 上传通话记录
        val uploadCallSuccess = NetApi.uploadCall("", call)
        if (!uploadCallSuccess) {
            DBHelper.getInstance(context)?.saveCall(call)
        }
        Pair(uploadFileSuccess, uploadCallSuccess)
    }

    /**
     * 重新上传所有上传失败的通话记录和录音文件
     */
    suspend fun reUploadFail(context: Context) = withContext(Dispatchers.IO) {
        val dbHelper = DBHelper.getInstance(context) ?: return@withContext
        val calls = dbHelper.getCalls()
        for (call in calls) {
            val id = call.id ?: continue
            val recordingFileUrl = call.recordingFileUrl
            val recordingFile = call.recordingFile
            if (!recordingFileUrl.isNullOrEmpty()) {// 文件已经上传
                // 上传通话记录
                if (NetApi.uploadCall("", call)) {// 成功
                    dbHelper.deleteCallById(id)
                }
            } else if (!recordingFile.isNullOrEmpty()) {// 文件没有上传
                // 上传录音文件
                val url = NetApi.uploadFile("", File(recordingFile))
                if (url != null) {// 成功
                    call.recordingFileUrl = url
                    dbHelper.updateCallRecordingFileUrlById(id, url)
                }
                // 上传通话记录
                if (NetApi.uploadCall("", call)) {// 成功
                    dbHelper.deleteCallById(id)
                }
            } else {
                // 上传通话记录
                if (NetApi.uploadCall("", call)) {// 成功
                    dbHelper.deleteCallById(id)
                }
            }
        }
    }
}
