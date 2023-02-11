package com.like.chengdu.call

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File

/**
 * 上传文件工具类
 */
object UploadUtils {
    private val mOkHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder().build()
    }

    /**
     * 上传文件
     *
     * @param url   上传路径
     * @param file  需要上传的文件
     * @return 上传成功返回true，失败返回false
     */
    suspend fun upload(url: String?, file: File?): Boolean {
        if (url.isNullOrEmpty() || file == null || !file.exists() || file.isDirectory || file.length() <= 0) {
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                val data = file.readBytes()
                val requestBody: RequestBody = data.toRequestBody(null, 0, data.size)
                val request: Request = Request.Builder().url(url).method("PUT", requestBody).build()

                @Suppress("BlockingMethodInNonBlockingContext")
                val response: Response = mOkHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

}
