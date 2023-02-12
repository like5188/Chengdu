package com.like.chengdu.call

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 通话录音工具类
 */
object CallRecordingUtils {

    suspend fun getCallRecordingFile(config: ScanCallRecordConfig?): File? = withContext(Dispatchers.IO) {
        config ?: return@withContext null
        try {
            val parent = Environment.getExternalStorageDirectory()
            val dir = File(parent, config.filePath)
            if (!dir.exists() || !dir.isDirectory) {
                return@withContext null
            }
            dir.listFiles()?.firstOrNull {
                isValidFile(it) && isCallRecordingFile(it, config)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isValidFile(file: File?): Boolean =
        file != null && file.isFile && file.exists() && file.length() > 0

    private fun isCallRecordingFile(file: File, config: ScanCallRecordConfig): Boolean {
        return file.name.lowercase(Locale.getDefault()).endsWith(config.fileSuffix) &&
                System.currentTimeMillis() - file.lastModified() > config.modifyTimeError * 1000
    }

}
