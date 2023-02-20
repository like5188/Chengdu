package com.like.chengdu.call

import com.mobile.ffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * 音频格式转换工具类
 */
internal object AudioConverter {

    suspend fun convert(file: File, format: String): File = withContext(Dispatchers.IO) {
        if (!file.exists() || !file.canRead()) {
            return@withContext file
        }
        val convertedFile = replaceSuffix(file, format)
        val cmd = arrayOf("-y", "-i", file.path, convertedFile.path)
        val result = try {
            FFmpeg.execute(cmd)
        } catch (e: Exception) {
            -1
        }
        if (result == 0) convertedFile else file
    }

    private fun replaceSuffix(originalFile: File, format: String): File {
        val f = originalFile.path.split(".").toTypedArray()
        val filePath = originalFile.path.replace(f[f.size - 1], format.lowercase(Locale.getDefault()))
        return File(filePath)
    }

}
