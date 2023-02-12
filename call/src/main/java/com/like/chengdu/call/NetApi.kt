package com.like.chengdu.call

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * 网络接口
 */
@Suppress("BlockingMethodInNonBlockingContext")
object NetApi {
    private val mOkHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder().build()
    }

    /**
     * 获取扫描录音文件的配置
     *
     * @param romName           手机系统名称
     * @param romVersion        手机系统版本
     * @param sdkVersion        Android Sdk 版本
     *
     * @return 上传成功返回true，失败返回false
     */
    suspend fun getScanCallRecordConfig(url: String?, romName: String, romVersion: String, sdkVersion: Int): ScanCallRecordConfig? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return ScanCallRecordConfig("/Music/Recordings/Call Recordings/", 3, "yyMMddHHmm", "-", ".amr", 1)

        return withContext(Dispatchers.IO) {
            try {
                val body: MultipartBody = MultipartBody.Builder()
                    .setType("multipart/form-data".toMediaType())
                    .addFormDataPart("romName", romName)
                    .addFormDataPart("romVersion", romVersion)
                    .addFormDataPart("sdkVersion", sdkVersion.toString())
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

    /**
     * 上传文件
     *
     * @param url   上传路径
     * @param file  需要上传的文件
     * @return 上传成功返回true，失败返回false
     */
    suspend fun uploadFile(url: String?, file: File?): Boolean {
        if (url.isNullOrEmpty() || file == null || !file.exists() || file.isDirectory || file.length() <= 0) {
            return false
        }
        return withContext(Dispatchers.IO) {
            try {
                val data = file.readBytes()
                val requestBody: RequestBody = data.toRequestBody(null, 0, data.size)
                val request: Request = Request.Builder().url(url).method("PUT", requestBody).build()
                val response: Response = mOkHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
