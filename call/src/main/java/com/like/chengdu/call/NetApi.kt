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
    private val mOkHttpClient by lazy {
        OkHttpClient().newBuilder().build()
    }
    private val mGson by lazy {
        Gson()
    }

    /**
     * 获取扫描录音文件的配置
     *
     * @param romName           手机系统名称
     * @param romVersion        手机系统版本
     * @param sdkVersion        Android Sdk 版本
     */
    suspend fun getScanCallRecordingConfig(
        url: String?,
        romName: String,
        romVersion: String,
        sdkVersion: Int
    ): ScanCallRecordingConfig? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return ScanCallRecordingConfig("/Music/Recordings/Call Recordings/", ".amr", 5000)

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
                    val resultModel: ResultModel<ScanCallRecordingConfig> = mGson.fromJson(
                        response.body?.string(),
                        ResultModel::class.java
                    ) as ResultModel<ScanCallRecordingConfig>
                    resultModel.data
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 上传通话记录
     *
     * @param call  通话记录
     */
    suspend fun uploadCall(
        url: String?,
        call: Call?
    ): Boolean {
        if (url.isNullOrEmpty() || call == null) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val body: MultipartBody = MultipartBody.Builder()
                    .setType("multipart/form-data".toMediaType())
                    .addFormDataPart("name", call.name.toString())
                    .addFormDataPart("number", call.number.toString())
                    .addFormDataPart("date", call.date.toString())
                    .addFormDataPart("duration", call.duration.toString())
                    .build()
                val request: Request = Request.Builder()
                    .post(body)
                    .url(url)
                    .build()

                val response: Response = mOkHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * 上传文件
     *
     * @param url   上传路径
     * @param file  需要上传的文件
     * @return 文件网络地址
     */
    suspend fun uploadFile(url: String?, file: File?): String? {
        if (url.isNullOrEmpty() || file == null || !file.exists() || file.isDirectory || file.length() <= 0) {
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val data = file.readBytes()
                val requestBody: RequestBody = data.toRequestBody(null, 0, data.size)
                val request: Request = Request.Builder().url(url).method("PUT", requestBody).build()
                val response: Response = mOkHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val resultModel: ResultModel<String> = mGson.fromJson(
                        response.body?.string(),
                        ResultModel::class.java
                    ) as ResultModel<String>
                    resultModel.data
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

class ResultModel<T>(val code: Int, val msg: String, val data: T?)
