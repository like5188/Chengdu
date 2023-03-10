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
    suspend fun upload(context: Context, localCall: LocalCall, file: File?): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        var uploadFileSuccess = false
        if (file != null) {
            localCall.recordingFile = file.absolutePath
            // 上传录音文件
            val url = NetApi.uploadFile("", file)
            uploadFileSuccess = url != null
            if (url != null) {// 成功
                localCall.recordingFileUrl = url
            }
        }
        // 上传通话记录
        val uploadCallSuccess = NetApi.uploadLocalCall("", localCall)
        if (!uploadCallSuccess) {
            DBHelper.getInstance(context)?.saveLocalCall(localCall)
        }
        Pair(uploadFileSuccess, uploadCallSuccess)
    }

    /**
     * 重新上传所有上传失败的通话记录和录音文件
     */
    suspend fun reUploadFail(context: Context) = withContext(Dispatchers.IO) {
        val dbHelper = DBHelper.getInstance(context) ?: return@withContext
        val localCalls = dbHelper.getLocalCalls()
        for (localCall in localCalls) {
            val id = localCall.id ?: continue
            val recordingFileUrl = localCall.recordingFileUrl
            val recordingFile = localCall.recordingFile
            if (!recordingFile.isNullOrEmpty() && recordingFileUrl.isNullOrEmpty()) {// 有录音文件，并且没有成功上传
                // 上传录音文件
                val url = NetApi.uploadFile("", File(recordingFile))
                if (url != null) {// 成功
                    localCall.recordingFileUrl = url
                    dbHelper.updateCallRecordingFileUrlById(id, url)
                }
            }
            // 上传通话记录
            if (NetApi.uploadLocalCall("", localCall)) {// 成功
                dbHelper.deleteLocalCallById(id)
            }
        }
    }

}
