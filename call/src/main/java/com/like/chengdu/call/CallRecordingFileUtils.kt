package com.like.chengdu.call

import android.Manifest
import android.annotation.SuppressLint
import android.os.Environment
import android.os.FileObserver
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.*

/**
 * 通话录音文件相关的工具类
 */
class CallRecordingFileUtils {
    private val fileObservers = mutableListOf<CallRecordingDirObserver>()

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    fun init(config: ScanCallRecordingConfig) {
        if (fileObservers.isNotEmpty()) return
        val parent = Environment.getExternalStorageDirectory()
        val filePaths = config.getFilePaths()
        filePaths.filter {// 获取有效文件夹
            val dir = try {
                File(parent, it)
            } catch (e: Exception) {
                null
            }
            dir != null && dir.exists() && dir.isDirectory
        }.map {
            File(parent, it)
        }.forEach { dir ->
            println("监听文件夹：$dir")
            fileObservers.add(CallRecordingDirObserver(dir, config))
        }
    }

    /**
     * 开始监听录音文件夹。(拨打电话前调用)
     */
    fun startWatching() {
        fileObservers.forEach {
            it.startWatching()
        }
    }

    /**
     * 如果有录音文件目录，那么各个录音文件目录下各取一个录音文件
     */
    suspend fun getCallRecordingFile(): List<File> {
        val result = mutableListOf<File>()
        fileObservers.forEach {
            val file = it.getCallRecordingFile()
            if (file != null) {
                result.add(file)
            }
        }
        return result
    }

}

/**
 * 监听可能的录音文件目录
 */
@SuppressLint("NewApi")
internal class CallRecordingDirObserver(
    private val dir: File,
    private val config: ScanCallRecordingConfig,
) : FileObserver(dir, CREATE or CLOSE_WRITE) {
    private var mAction: Int? = null
    private var callRecordingFile: File? = null

    override fun onEvent(event: Int, path: String?) {
        path ?: return
        val file = try {
            File(dir, path)
        } catch (e: Exception) {
            null
        }
        val action = event and ALL_EVENTS
        if (action == CREATE) {
            if (callRecordingFile == null) {
                println("找到录音文件:$file")
                // 找到录音文件
                if (isValidFile(file) && isValidSuffix(file!!, config)) {
                    callRecordingFile = file
                    mAction = CREATE
                }
            }
        } else if (action == CLOSE_WRITE) {
            if (callRecordingFile == file) {
                println("录音文件写入完毕:$file")
                mAction = CLOSE_WRITE
            }
        }
    }

    /**
     * 开始监听录音文件夹。(拨打电话前调用)
     */
    override fun startWatching() {
        mAction = null
        callRecordingFile = null
        super.startWatching()
    }

    override fun stopWatching() {
        mAction = null
        callRecordingFile = null
        super.stopWatching()
    }

    /**
     * 获取录音文件，并停止监听目录。5秒未获取到，就返回null
     */
    suspend fun getCallRecordingFile(): File? = withContext(Dispatchers.IO) {
        try {
            if (callRecordingFile != null) {
                withTimeout(5000) {
                    while (mAction != CLOSE_WRITE) {
                        delay(100)
                    }
                }
            }
            callRecordingFile
        } catch (e: Exception) {
            null
        } finally {
            stopWatching()
        }
    }

    private fun isValidFile(file: File?): Boolean = file != null && file.isFile && file.exists()

    private fun isValidSuffix(file: File, config: ScanCallRecordingConfig): Boolean {
        val fileSuffixes = config.getFileSuffixes()
        val fileName = file.name.lowercase(Locale.getDefault())
        return fileSuffixes.any { fileName.endsWith(it) }
    }

}

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
class ScanCallRecordingConfig(
    private val filePaths: Array<String>? = null,// 通话录音文件路径。
    private val fileSuffixes: Array<String>? = null,// 通话录音文件后缀。
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

}
