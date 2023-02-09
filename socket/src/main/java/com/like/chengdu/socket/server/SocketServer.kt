package com.like.chengdu.socket.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketServer : Service() {

    var serverAllowed: Boolean = true

    private var runnable = Runnable {
        val serverSocket = ServerSocket(9999)
        val accept: Socket = serverSocket.accept()
        Thread { response(accept) }.start()
    }

    override fun onCreate() {
        super.onCreate()
        Thread(runnable).start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun response(accept: Socket) {
        try {
            //从客户端接收的信息
            val bufferedReaderIn = BufferedReader(InputStreamReader(accept.getInputStream()))
            //发送信息给客户端
            val out: PrintWriter = PrintWriter(BufferedWriter(OutputStreamWriter(accept.getOutputStream())), true)
            while (serverAllowed) {
                val msg = bufferedReaderIn.readLine()
                if (TextUtils.isEmpty(msg)) {
                    println("收到客户端的信息为空，断开连接")
                    break
                }
                println("收到客户端的信息： $msg")
                val msgOp = "加工从客户端的信息： $msg"
                out.println(msgOp);
            }
            println("关闭服务")
            bufferedReaderIn.close()
            out.close()
            accept.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        serverAllowed = false
    }
}