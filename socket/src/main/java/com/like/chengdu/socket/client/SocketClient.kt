package com.like.chengdu.socket.client

import android.os.SystemClock
import java.io.*
import java.net.Socket

class SocketClient {
    private var isFinishing = false

    fun connectSocketServer() {
        var read: BufferedReader? = null
        var write: PrintWriter? = null
        var socket: Socket? = null
        try {
            if (socket == null) {
                socket = Socket("localhost", 9999)
            }
            while (!isFinishing) {
                //1、发送数据
                if (write == null) {
                    write = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true) //发送数据
                }
                write.println("客户端发出信息")

                //2、 接收服务器端的消息
                if (read == null) {
                    read = BufferedReader(InputStreamReader(socket.getInputStream()))
                }
                val msg: String = read.readLine()
                println("客户端接收信息：$msg")
                SystemClock.sleep(1000)
            }
            println("客户端关闭")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            read?.close()
            write?.close()
            socket?.close()
        }
    }
}