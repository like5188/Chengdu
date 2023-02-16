package com.like.chengdu.call

import android.Manifest
import android.os.Environment
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 通话录音工具类
 */
object CallRecordingUtils {

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    suspend fun getLastModifiedCallRecordingFile(config: ScanCallRecordConfig?): File? =
        withContext(Dispatchers.IO) {
            config ?: return@withContext null
            try {
                val parent = Environment.getExternalStorageDirectory()
                val dir = File(parent, config.filePath)
                if (!dir.exists() || !dir.isDirectory) {
                    return@withContext null
                }
                dir.listFiles()?.sortedBy {
                    it.lastModified()
                }?.firstOrNull {
                    isValidFile(it) && isValidCallRecordingFile(it, config)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun isValidFile(file: File?): Boolean = file != null && file.isFile && file.exists()

    private fun isValidCallRecordingFile(file: File, config: ScanCallRecordConfig): Boolean {
        return file.name.lowercase(Locale.getDefault()).endsWith(config.fileSuffix) &&
                System.currentTimeMillis() - file.lastModified() <= config.modifyTimeError
    }

}

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
data class ScanCallRecordConfig(
    val filePath: String,// 通话录音文件路径。例如："/Music/Recordings/Call Recordings/"
    val fileSuffix: String,// 通话录音文件后缀
    val modifyTimeError: Int,// 修改时间误差值。毫秒
)
