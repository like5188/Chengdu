package com.like.chengdu.call.java;

import com.mobile.ffmpeg.FFmpeg;

import java.io.File;
import java.util.Locale;

/**
 * 音频格式转换工具类
 */
public class AudioConverter {
    public static File convertToWav(File file) {
        if (file == null) {
            return null;
        }
        String filePath = file.getPath();
        // 录音文件格式作最好都转为：mp3,  wav
        if (filePath.endsWith(".mp3") || filePath.endsWith(".wav")) {
            return file;
        }
        return convert(file, "wav");
    }

    private static File convert(File file, String format) {
        if (!file.exists() || !file.canRead()) {
            return file;
        }
        File convertedFile = replaceSuffix(file, format);
        String[] cmd = new String[]{"-y", "-i", file.getPath(), convertedFile.getPath()};
        int result = -1;
        try {
            result = FFmpeg.execute(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result == 0 ? convertedFile : file;
    }

    private static File replaceSuffix(File originalFile, String format) {
        String[] f = originalFile.getPath().split(".");
        String filePath = originalFile.getPath().replace(f[f.length - 1], format.toLowerCase(Locale.ROOT));
        return new File(filePath);
    }
}
