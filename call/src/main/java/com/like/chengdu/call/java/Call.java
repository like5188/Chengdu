package com.like.chengdu.call.java;

import android.database.Cursor;
import android.provider.CallLog;

import java.text.SimpleDateFormat;

public class Call {
    public int id;
    public String name;//联系人
    public String number;//被叫号码
    public long dateOfCallOccurred;//开始时间
    public int duration;//通话时长 接通才有，接通后到挂断的时间。秒

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Call(
            int id,
            String name,
            String number,
            long dateOfCallOccurred,
            int duration
    ) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.dateOfCallOccurred = dateOfCallOccurred;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "id=$id,\n" +
                "联系人=$name,\n" +
                "被叫号码=$number,\n" +
                "开始时间=${formatTime(dateOfCallOccurred)},\n" +
                "通话时长=${duration} 秒";
    }

    protected String formatTime(long time) {
        if (time <= 0) {
            return "";
        }
        try {
            return sdf.format(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] getProjection() {
        return new String[]{CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION};
    }

    public static Call parse(Cursor cursor) {
        int idColumnIndex = cursor.getColumnIndex(CallLog.Calls._ID);
        int nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
        int durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
        return new Call(
                idColumnIndex == -1 ? -1 : cursor.getInt(idColumnIndex),
                nameColumnIndex == -1 ? null : cursor.getString(nameColumnIndex),
                numberColumnIndex == -1 ? null : cursor.getString(numberColumnIndex),
                dateColumnIndex == -1 ? -1L : cursor.getLong(dateColumnIndex),
                durationColumnIndex == -1 ? -1 : cursor.getInt(durationColumnIndex)
        );
    }

}