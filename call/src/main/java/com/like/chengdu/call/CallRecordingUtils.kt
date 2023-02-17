package com.like.chengdu.call

import android.Manifest
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
            try {
                val c = config ?: ScanCallRecordingConfig()
                Log.i("TAG", c.toString())
                val parent = Environment.getExternalStorageDirectory()
                val filePaths = c.getFilePaths()
                val scanDelay = c.getScanDelay()
                val currentTimeMillis = System.currentTimeMillis()
                delay(scanDelay)
                filePaths.forEach { filePath ->
                    val dir = File(parent, filePath)
                    if (dir.exists() && dir.isDirectory) {
                        val file = dir.listFiles()?.sortedByDescending {
                            it.lastModified()
                        }?.firstOrNull {
                            isValidFile(it) && isValidCallRecordingFile(
                                it,
                                c,
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
        val fileSuffixes = config.getFileSuffixes()
        val modifyTimeError = config.getModifyTimeError()
        val fileName = file.name.lowercase(Locale.getDefault())
        return fileSuffixes.any { fileName.contains(it) } &&
                currentTimeMillis - file.lastModified() <= modifyTimeError
    }

}

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
data class ScanCallRecordingConfig(
    private val filePaths: Array<String>? = null,// 通话录音文件路径。
    private val fileSuffixes: Array<String>? = null,// 通话录音文件后缀。
    private val modifyTimeError: Long? = null,// 修改时间与扫描文件时间的误差值。毫秒
    private val scanDelay: Long? = null,// 扫描延迟时间。毫秒
) {
    fun getFilePaths(): Array<String> = filePaths ?: arrayOf(
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

    fun getFileSuffixes(): Array<String> =
        fileSuffixes ?: arrayOf(".mp3", ".wav", ".3gp", ".amr", ".3gpp")

    fun getModifyTimeError(): Long = modifyTimeError ?: 3000L

    fun getScanDelay(): Long = scanDelay ?: 1000L

}
