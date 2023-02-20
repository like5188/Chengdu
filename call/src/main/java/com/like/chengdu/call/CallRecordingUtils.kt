package com.like.chengdu.call

import android.Manifest
import android.content.Context
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
    suspend fun getLastModifiedCallRecordingFile(context: Context, config: ScanCallRecordingConfig?): File? =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val c = config ?: ScanCallRecordingConfig()
                Log.d("TAG", c.toString())
                val parent = Environment.getExternalStorageDirectory()
                val filePaths = c.getFilePaths()
                val scanDelay = c.getScanDelay()
                val currentTimeMillis = System.currentTimeMillis()
                delay(scanDelay)
                filePaths.forEach { filePath ->
                    Log.v("TAG", "扫描：$filePath")
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
                            Log.i("TAG", "扫描到文件：${file.absolutePath}")
                            return@withContext convertFile(context, file)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                Log.i("TAG", "扫描耗时：${System.currentTimeMillis() - startTime}毫秒")
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
        return fileSuffixes.any { fileName.endsWith(it) } &&
                currentTimeMillis - file.lastModified() <= modifyTimeError
    }

    private suspend fun convertFile(context: Context, file: File): File = withContext(Dispatchers.IO) {
        val extension = file.extension
        // 录音文件格式作最好都转为：mp3,  wav
        if (extension == "mp3" || extension == "wav") {
            return@withContext file
        }
        AudioConverter.convert(file, "wav")
    }

}

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
class ScanCallRecordingConfig(
    private val filePaths: Array<String>? = null,// 通话录音文件路径。
    private val fileSuffixes: Array<String>? = null,// 通话录音文件后缀。
    private val modifyTimeError: Long? = null,// 修改时间与扫描文件时间的误差值。毫秒
    private val scanDelay: Long? = null,// 扫描延迟时间。毫秒
) {
    fun getFilePaths(): Array<String> = filePaths ?: arrayOf(
        "/Sounds/CallRecord",
        "/record",
        "/Record",
        "/sounds/callrecord",
        "/PhoneRecord",
        "/Music Recordings",
        "/MIUI/sound_recorder/call_rec",
        "/MIUI/sound_recorder",
        "/MIUI/sound_recorder/call_rec2",
        "/MIUI/sound_recorder/call",
        "/Download/录音",
        "/Recorder",
        "/Recorder/call",
        "/Recordings",
        "/Recordings/Call Recordings",
        "/Record/Call",
        "/录音/通话录音",
        "/Recordings/Record/Call",
        "/Call",
        "/Recordings/Call",
        "/Music/Record/SoundRecord",
        "/Record/PhoneRecord",
        "/Music/Recordings/Call Recordings",
        "/Sounds",
    )

    fun getFileSuffixes(): Array<String> =
        fileSuffixes ?: arrayOf(".mp3", ".wav", ".3gp", ".amr", ".3gpp", ".act", ".wma")

    fun getModifyTimeError(): Long = modifyTimeError ?: 5000L

    fun getScanDelay(): Long = scanDelay ?: 500L

}
