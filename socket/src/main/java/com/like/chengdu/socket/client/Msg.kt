package com.like.chengdu.socket.client

data class Msg(val tenantUserId: String?, val msgType: MsgType?, val msgContent: String?)

/*
HEART（心跳）
ONLINE（上线消息）
OFFLINE（下线消息）
SYS_NOTICE（系统通知）
INSIDE_MSG（站内信）
PHONE（发起通话）
 */
enum class MsgType {
    HEART, ONLINE, OFFLINE, SYS_NOTICE, INSIDE_MSG, PHONE
}