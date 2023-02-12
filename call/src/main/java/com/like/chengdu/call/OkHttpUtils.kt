package com.like.chengdu.call

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType


/**
 * 上传文件工具类
 */
object OkHttpUtils {
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
    suspend fun post(url: String?, name: String, version: String): ScanCallRecordConfig? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return ScanCallRecordConfig("/Music/Recordings/Call Recordings/", 3, "yyMMddHHmm", "-", ".amr", 1)

        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(Dispatchers.IO) {
            try {
                val body: MultipartBody = MultipartBody.Builder()
                    .setType("multipart/form-data".toMediaType())
                    .addFormDataPart("name", name)
                    .addFormDataPart("version", version)
                    .build()
                val request: Request = Request.Builder()
                    .post(body)
                    .url(url)
                    .build()

                val response: Response = mOkHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    Gson().fromJson(response.body?.string(), ScanCallRecordConfig::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}
