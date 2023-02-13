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

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
data class ScanCallRecordConfig(
    val filePath: String,// 通话录音文件路径。例如："/Music/Recordings/Call Recordings/"
    val scanType: Int,// 扫描类型。0：姓名；1：创建时间；2：修改时间；3：姓名+创建时间；4：姓名+修改时间
    val fileNameTimeFormat: String,// 如果文件名包含时间，需要指定这个时间格式
    val fileNameSeparator: String,// 如果文件名是组合方式（scanType = 3、4），需要指定这个分隔符
    val fileSuffix: String,// 通话录音文件后缀
    val modifyTimeError: Int,// 修改时间误差值。秒
)
