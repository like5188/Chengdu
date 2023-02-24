package com.like.chengdu.call

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // 创建通话记录表，此表存储了上传失败的通话记录
        db.execSQL("CREATE TABLE IF NOT EXISTS $LOCAL_CALL_TABLE_NAME(${LocalCall.getCreateDbSql()})")
    }

    suspend fun saveLocalCall(localCall: LocalCall): Boolean = withContext(Dispatchers.IO) {
        writableDatabase.insert(LOCAL_CALL_TABLE_NAME, null, localCall.getContentValues()) != -1L
    }

    suspend fun getLocalCalls(): List<LocalCall> = withContext(Dispatchers.IO) {
        val result = mutableListOf<LocalCall>()
        readableDatabase.rawQuery("SELECT * FROM $LOCAL_CALL_TABLE_NAME", arrayOf())?.use {
            while (it.moveToNext()) {
                result.add(LocalCall.parse(it))
            }
        }
        result
    }

    suspend fun deleteLocalCallById(id: Int) = withContext(Dispatchers.IO) {
        writableDatabase.delete(LOCAL_CALL_TABLE_NAME, "id=?", arrayOf(id.toString()))
    }

    suspend fun updateCallRecordingFileUrlById(id: Int, recordingFileUrl: String): Boolean =
        withContext(Dispatchers.IO) {
            if (recordingFileUrl.isEmpty()) return@withContext false
            val values = ContentValues()
            values.put("recordingFileUrl", recordingFileUrl)
            writableDatabase.update(LOCAL_CALL_TABLE_NAME, values, "id=?", arrayOf(id.toString())) > 0
        }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        private const val DATABASE_NAME = "call.db"
        private const val LOCAL_CALL_TABLE_NAME = "local_call"
        private const val DATABASE_VERSION = 1

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DBHelper? = null
        fun getInstance(context: Context): DBHelper? {
            if (INSTANCE == null) {
                synchronized(DBHelper::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = DBHelper(context.applicationContext)
                    }
                }
            }
            return INSTANCE
        }
    }

}
