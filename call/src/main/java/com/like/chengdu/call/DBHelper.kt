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
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS call(" +
                    "id INTEGER PRIMARY KEY," +
                    "name VARCHAR," +
                    "number VARCHAR," +
                    "dateOfCallOccurred INTEGER," +
                    "duration INTEGER," +
                    "recordingFile VARCHAR," +
                    "recordingFileUrl VARCHAR," +
                    "dateOfCallConnected INTEGER," +
                    "dateOfCallHungUp INTEGER," +
                    "reasonOfHungUp VARCHAR," +
                    "callState VARCHAR," +
                    "xxx1  INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    suspend fun getCalls(): List<Call> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Call>()
        readableDatabase.rawQuery("SELECT * FROM call", arrayOf())?.use {
            if (!it.moveToFirst()) {
                return@use
            }
            while (!it.isAfterLast) {
                result.add(Call.parse(it))
                it.moveToNext()
            }
        }
        result
    }

    suspend fun deleteCallById(id: Int) = withContext(Dispatchers.IO) {
        writableDatabase.delete("call", "id=?", arrayOf(id.toString()))
    }

    suspend fun saveCall(call: Call): Boolean = withContext(Dispatchers.IO) {
        val cv = ContentValues()
        cv.put("id", call.id)
        cv.put("name", call.name)
        cv.put("number", call.number)
        cv.put("dateOfCallOccurred", call.dateOfCallOccurred)
        cv.put("duration", call.duration)
        cv.put("recordingFile", call.recordingFile)
        cv.put("recordingFileUrl", call.recordingFileUrl)
        cv.put("dateOfCallConnected", call.dateOfCallConnected)
        cv.put("dateOfCallHungUp", call.dateOfCallHungUp)
        cv.put("reasonOfHungUp", call.reasonOfHungUp)
        cv.put("callState", call.callState)
        cv.put("xxx1", call.xxx1)
        writableDatabase.insert("call", null, cv) != -1L
    }

    suspend fun updateCallRecordingFileUrlById(id: Int, recordingFileUrl: String): Boolean = withContext(Dispatchers.IO) {
        if (recordingFileUrl.isEmpty()) return@withContext false
        val values = ContentValues()
        values.put(recordingFileUrl, recordingFileUrl)
        writableDatabase.update("call", values, "id=?", arrayOf(id.toString())) > 0
    }

    companion object {
        private const val DATABASE_NAME = "call.db"
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
