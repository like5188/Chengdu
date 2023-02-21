package com.like.chengdu.call

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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
     */
    suspend fun getScanCallRecordingConfig(url: String?): ScanCallRecordingConfig? {
        if (url.isNullOrEmpty()) {
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val request: Request = Request.Builder()
                    .get()
                    .url(url)
                    .build()

                val response: Response = mOkHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    mGson.fromJson(
                        response.body?.string(),
                        ScanCallRecordingConfig::class.java
                    )
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
     */
    suspend fun uploadLocalCall(
        url: String?,
        localCall: LocalCall?
    ): Boolean {
        return false// todo 删除假数据
        if (url.isNullOrEmpty() || localCall == null) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val body = mGson.toJson(localCall).toRequestBody("application/json;charset=utf-8".toMediaType())
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
        return "http://www.ytmp3.cn/down/57799.mp3"// todo 删除假数据
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
