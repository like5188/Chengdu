package com.like.chengdu.call.java;

import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.IOException;

/**
 * 音频工具类
 */
public class AudioUtils {
    private String curUrl;
    private final MediaPlayer mediaPlayer;

    public AudioUtils() {
        mediaPlayer = new MediaPlayer();
    }

    public void start(String url) {
        if (url == null || TextUtils.isEmpty(url)) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            if (url.equals(curUrl)) {
                mediaPlayer.start();
            } else {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    curUrl = url;
                    mediaPlayer.start();
                });
            }
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void destroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

}
