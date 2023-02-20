package com.like.chengdu.call

import android.Manifest
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 通话录音文件相关的工具类
 */
class CallRecordingFileUtils {
    private var callRecordingFile: File? = null
    private lateinit var config: ScanCallRecordingConfig
    private val fileObservers = mutableListOf<FileObserver>()

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    fun init(config: ScanCallRecordingConfig) {
        callRecordingFile = null
        this.config = config
        val parent = Environment.getExternalStorageDirectory()
        val filePaths = config.getFilePaths()
        filePaths.forEach {
            val dir = File(parent, it)
            if (dir.exists() && dir.isDirectory) {
                val observer = object : FileObserver(dir) {
                    override fun onEvent(event: Int, path: String?) {
                        path ?: return
                        val action = event and ALL_EVENTS
                        if (action != CREATE) return
                        val file = try {
                            File(dir, path)
                        } catch (e: Exception) {
                            null
                        } ?: return
                        println("action:$action file:$file")
                        if (isValidFile(file) && isValidSuffix(file, config)) {
                            // 找到录音文件
                            callRecordingFile = file
                            // 停止监听
                            fileObservers.forEach {
                                it.stopWatching()
                            }
                            Log.i("TAG", "找到录音文件：${file.absolutePath}")
                        }
                    }
                }
                fileObservers.add(observer)
            }
        }
    }

    private fun isValidFile(file: File?): Boolean = file != null && file.isFile && file.exists()

    private fun isValidSuffix(file: File, config: ScanCallRecordingConfig): Boolean {
        val fileSuffixes = config.getFileSuffixes()
        val fileName = file.name.lowercase(Locale.getDefault())
        return fileSuffixes.any { fileName.endsWith(it) }
    }

    private suspend fun convertFile(file: File?): File? = withContext(Dispatchers.IO) {
        file ?: return@withContext null
        val extension = file.extension
        // 录音文件格式作最好都转为：mp3,  wav
        if (extension == "mp3" || extension == "wav") {
            return@withContext file
        }
        AudioConverter.convert(file, "wav")
    }

    /**
     * 开始监听录音文件夹。(拨打电话前调用)
     */
    fun start() {
        callRecordingFile = null
        fileObservers.forEach {
            it.startWatching()
        }
    }

    /**
     * 停止监听录音文件夹。(挂断电话后调用)
     */
    suspend fun stop(): File? = withContext(Dispatchers.IO) {
        val currentTimeMillis = System.currentTimeMillis()
        fileObservers.forEach {
            it.stopWatching()
        }
        val file = callRecordingFile ?: return@withContext null
        val scanDelay = config.getScanDelay()
        val modifyTimeError = config.getModifyTimeError()
        delay(scanDelay)
        if (currentTimeMillis - file.lastModified() <= modifyTimeError) {
            convertFile(file)
        } else {
            null
        }
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
