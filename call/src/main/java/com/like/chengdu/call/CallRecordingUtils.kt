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
                isCallRecordingFile(it, config)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isCallRecordingFile(file: File?, config: ScanCallRecordConfig): Boolean {
        return file != null &&
                file.isFile &&
                file.exists() &&
                file.length() > 0 &&
                file.name.lowercase(Locale.getDefault()).endsWith(config.fileSuffix) &&
                file.lastModified() - time > -20 * 1000
    }

}
