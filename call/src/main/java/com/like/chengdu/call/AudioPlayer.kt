package com.like.chengdu.call

import android.media.MediaPlayer

class AudioPlayer {
    private val mediaPlayer by lazy {
        MediaPlayer()
    }
    private var curUrl = ""

    fun start(url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        if (!mediaPlayer.isPlaying) {
            if (url == curUrl) {
                mediaPlayer.start()
            } else {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    curUrl = url
                    mediaPlayer.start()
                }
            }
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun destroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

}
