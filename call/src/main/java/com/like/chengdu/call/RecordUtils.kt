package com.like.chengdu.call

import android.content.Context
import android.os.Environment
import android.util.Log
import com.like.chengdu.call.RomUtils.isHuawei
import com.like.chengdu.call.RomUtils.isMeizu
import com.like.chengdu.call.RomUtils.isOppo
import com.like.chengdu.call.RomUtils.isSamsung
import com.like.chengdu.call.RomUtils.isVivo
import com.like.chengdu.call.RomUtils.isXiaomi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通话录音工具类
 */
class RecordUtils(context: Context) {
    companion object {
        private const val record_dir_key = "record_dir_key"
    }

    init {
        SPUtils.getInstance().init(context)
    }

    //指定系统下的系统录音文件路径
    private val systemRecordPath: String
        get() {
            val parent = Environment.getExternalStorageDirectory()
            var child: File? = null
            when {
                isHuawei -> {
                    child = File(parent, "record")
                    if (!child.exists()) {
                        child = File(parent, "Sounds/CallRecord")
                    }
                }
                isXiaomi -> {
                    child = File(parent, "MIUI/sound_recorder/call_rec")
                }
                isMeizu -> {
                    child = File(parent, "Recorder")
                }
                isOppo -> {
                    child = File(parent, "Recordings/Call Recordings")
                    if (!child.exists()) {
                        child = File(parent, "Recordings")
                    }
                }
                isVivo -> {
                    child = File(parent, "Record/Call")
                }
                isSamsung -> {
                    child = File(parent, "Sounds")
                }
            }
            return if (child == null || !child.exists()) {
                ""
            } else child.absolutePath
        }

    //其它常用的系统录音文件路径
    private val otherRecordPaths: List<String>
        get() {
            val parent = Environment.getExternalStorageDirectory()
            val list = mutableListOf<String>()
            //oppp android-10 手机存储系统录音
            var file = File(parent, "Music/Recordings/Call Recordings")
            if (file.exists()) {
                list.add(file.absolutePath)
            }
            file = File(parent, "PhoneRecord")
            if (file.exists()) {
                list.add(file.absolutePath)
            }
            // todo 添加其它系统录音文件夹
            return list
        }

    suspend fun getLatestRecordFile(): File? = withContext(Dispatchers.IO) {
        try {
            val time: Long = Calendar.getInstance().timeInMillis
            var dir: File
            //使用记录下的文件夹下搜索
            var recordDir = SPUtils.getInstance().get(record_dir_key, "")
            Log.e("RecordHelper", "sp是否有缓存文件:$recordDir 当前时间:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(time))}")
            if (recordDir.isNotEmpty()) {
                //直接使用已存储文件夹下搜索
                val file = searchRecordFile(time, File(recordDir))
                if (file != null) {
                    return@withContext file
                }
            }
            //使用指定系统下的系统录音文件路径搜索
            recordDir = systemRecordPath
            if (recordDir.isNotEmpty()) {
                val file = searchRecordFile(time, File(recordDir))
                if (file != null) {
                    return@withContext file
                }
            }
            //使用其它常用的系统录音文件路径搜索
            val recordFiles = otherRecordPaths
            recordFiles.forEach {
                dir = File(it)
                val file = searchRecordFile(time, dir)
                if (file != null) {
                    return@withContext file
                }
            }
            //全局搜索录音文件夹并存储下来
            val file = searchRecordFile(time, Environment.getExternalStorageDirectory(), true)

            val time2: Long = Calendar.getInstance().timeInMillis
            Log.e("RecordHelper", "全局搜索录音文件夹所花时间:${time2 - time} 当前时间:${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(time2))}")
            return@withContext file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    /**
     * @param recursion  是否递归搜索dir下的所有文件夹
     */
    private fun searchRecordFile(time: Long, dir: File, recursion: Boolean = false): File? {
        if (dir.isDirectory && !isOtherRecordAppDir(dir)) {
            dir.listFiles()?.forEach {
                //20秒之内生成的文件 默认为当前的录音文件(TODO 这里如果需要更准确可以判断是否是录音,录音时长校对)
                if (isRecordFile(it, time)) {
                    SPUtils.getInstance().put(record_dir_key, it.parent)
                    return it
                }
                if (!recursion && it.isDirectory) {
                    return searchRecordFile(time, it, recursion)
                }
            }
        }
        return null
    }

    /**
     * 是否其它录音App的目录。
     */
    private fun isOtherRecordAppDir(dir: File): Boolean {
        val name = dir.name
        //加入一些会录音的app,会生成录音文件,防止使用其他录音文件而没有使用系统录音文件
        return "Android" == name ||
                "不是录音文件夹都可以写在这" == name
    }

    private fun isRecordFile(file: File?, time: Long): Boolean {
        //20秒之内生成的文件 默认为当前的录音文件(TODO 这里如果需要更准确可以判断是否是录音,录音时长校对)
        return file != null && file.isFile && file.exists() && file.length() > 0 && isRecordSuffix(file.name) && file.lastModified() - time > -20 * 1000
    }

    /**
     * 是否录音文件后缀
     */
    private fun isRecordSuffix(name: String?): Boolean {
        //录音文件匹配规则 -- 可以自行添加其他格式录音匹配
        if (name.isNullOrEmpty()) {
            return false
        }
        val lowercaseName = name.lowercase(Locale.getDefault())
        return lowercaseName.endsWith(".mp3") ||
                lowercaseName.endsWith(".wav") ||
                lowercaseName.endsWith(".3gp") ||
                lowercaseName.endsWith(".amr") ||
                lowercaseName.endsWith(".3gpp")
    }

}
