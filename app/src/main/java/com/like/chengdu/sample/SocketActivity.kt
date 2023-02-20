package com.like.chengdu.sample

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.chengdu.sample.databinding.ActivitySocketBinding
import com.like.chengdu.socket.client.NettyClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SocketActivity : AppCompatActivity() {
    private val mBinding: ActivitySocketBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_socket)
    }
    private val nettyClient by lazy {
        fun updateSocketMsg(newMsg: String) {
            lifecycleScope.launch(Dispatchers.Main) {
                val oldMsg = mBinding.etMsg.text?.toString()
                val text = if (oldMsg.isNullOrEmpty()) {
                    newMsg
                } else {
                    oldMsg + "\n" + newMsg
                }
                mBinding.etMsg.setText(text)
                mBinding.etMsg.setSelection(text.length, text.length)
            }
        }
        NettyClient(
            onConnected = {
                updateSocketMsg("已连接!")
            },
            onDisConnected = {
                updateSocketMsg("!!!未连接!!!")
            }
        ) {
            updateSocketMsg(it.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.etMsg.movementMethod = ScrollingMovementMethod.getInstance()
    }

    fun connect(view: View) {
        val host = mBinding.etHost.text?.toString()
        val port = mBinding.etPort.text?.toString()?.toInt()
        nettyClient.connect(host, port)
    }

    fun disconnect(view: View) {
        nettyClient.disconnect()
    }

    fun clearMsg(view: View) {
        mBinding.etMsg.setText("")
    }

    override fun onDestroy() {
        super.onDestroy()
        nettyClient.disconnect()
    }

}
