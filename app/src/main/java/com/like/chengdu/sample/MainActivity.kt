package com.like.chengdu.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.socket.client.SocketClient
import com.like.chengdu.socket.server.SocketServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startSocketServer(view: View) {
        startService(Intent(this, SocketServer::class.java))
    }

    fun startSocketClient(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            SocketClient().connectSocketServer()
        }
    }

}
