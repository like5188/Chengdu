package com.like.chengdu.call

import android.Manifest
import android.os.Environment
import android.os.FileObserver
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.*

/**
 * 通话录音文件相关的工具类
 */
class CallRecordingFileUtils {
    private var callRecordingFile: File? = null
    private lateinit var mConfig: ScanCallRecordingConfig
    private val fileObservers = mutableListOf<FileObserver>()

    @Volatile
    private var mAction: Int? = null

    private fun stopWatchingExclude(fileObserver: FileObserver) {
        fileObservers.forEach {
            if (it != fileObserver)
                it.stopWatching()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE])
    fun init(config: ScanCallRecordingConfig) {
        if (::mConfig.isInitialized) return
        callRecordingFile = null
        mConfig = config
        val parent = Environment.getExternalStorageDirectory()
        val filePaths = config.getFilePaths()
        filePaths.forEach {
            val dir = File(parent, it)
            if (dir.exists() && dir.isDirectory) {
                val observer = object : FileObserver(dir) {
                    override fun onEvent(event: Int, path: String?) {
                        path ?: return
                        val action = event and ALL_EVENTS
                        if (action == CREATE) {
                            val file = try {
                                File(dir, path)
                            } catch (e: Exception) {
                                null
                            }
                            println("CREATE file:$file")
                            // 找到录音文件
                            if (isValidFile(file) && isValidSuffix(file!!, config)) {
                                stopWatchingExclude(this)// 停止其它监听
                                mAction = CREATE
                            }
                        } else if (action == CLOSE_WRITE) {
                            if (mAction == CREATE) {
                                // 停止所有监听
                                fileObservers.forEach {
                                    it.stopWatching()
                                }
                                callRecordingFile = File(dir, path)
                                println("CLOSE_WRITE file:$callRecordingFile")
                                mAction = CLOSE_WRITE
                            }
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
        if (!::mConfig.isInitialized) return
        callRecordingFile = null
        mAction = null
        fileObservers.forEach {
            it.startWatching()
        }
    }

    /**
     * 停止监听录音文件夹。(销毁资源时调用)
     */
    fun destroy() {
        if (!::mConfig.isInitialized) return
        fileObservers.forEach {
            it.stopWatching()
        }
    }

    /**
     * 获取录音文件
     */
    suspend fun getCallRecordingFile(): File? = withContext(Dispatchers.IO) {
        if (!::mConfig.isInitialized) return@withContext null
        withTimeoutOrNull(5000) {
            while (mAction != FileObserver.CLOSE_WRITE) {
                delay(100)
            }
            convertFile(callRecordingFile)
        }
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
