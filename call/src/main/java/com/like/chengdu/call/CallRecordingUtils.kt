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
    suspend fun getLastModifiedCallRecordingFile(config: ScanCallRecordingConfig?): File? =
        withContext(Dispatchers.IO) {
            config ?: return@withContext null
            try {
                val parent = Environment.getExternalStorageDirectory()
                val filePathList = config.getFilePathList()
                val currentTimeMillis = System.currentTimeMillis()
                filePathList.forEach { filePath ->
                    val dir = File(parent, filePath)
                    if (dir.exists() && dir.isDirectory) {
                        val file = dir.listFiles()?.sortedByDescending {
                            it.lastModified()
                        }?.firstOrNull {
                            isValidFile(it) && isValidCallRecordingFile(
                                it,
                                config,
                                currentTimeMillis
                            )
                        }
                        if (file != null) {
                            return@withContext file
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }

    private fun isValidFile(file: File?): Boolean = file != null && file.isFile && file.exists()

    private fun isValidCallRecordingFile(
        file: File,
        config: ScanCallRecordingConfig,
        currentTimeMillis: Long
    ): Boolean {
        val fileSuffixList = config.getFileSuffixList()
        val modifyTimeError = config.getModifyTimeError()
        val fileName = file.name.lowercase(Locale.getDefault())
        return fileSuffixList.any { fileName.contains(it) } &&
                currentTimeMillis - file.lastModified() <= modifyTimeError
    }

}

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
data class ScanCallRecordingConfig(
    private val filePaths: String?,// 通话录音文件路径。例如："/Music/Recordings/Call Recordings/,/record"
    private val fileSuffixes: String?,// 通话录音文件后缀，例如：".mp3,.3gp"
    private val modifyTimeError: Long?,// 修改时间与扫描文件时间的误差值。毫秒
) {
    fun getFilePathList() =
        filePaths?.split(",") ?: listOf(
            "/record",
            "/Sounds/CallRecord",
            "/MIUI/sound_recorder/call_rec",
            "/Recorder",
            "/Recordings/Call Recordings",
            "/Music/Recordings/Call Recordings",
            "/Recordings",
            "/Record/Call",
            "/Sounds",
            "/PhoneRecord",
        )

    fun getFileSuffixList() =
        fileSuffixes?.split(",") ?: listOf(".mp3", ".wav", ".3gp", ".amr", ".3gpp")

    fun getModifyTimeError() = modifyTimeError ?: 3000L

}
