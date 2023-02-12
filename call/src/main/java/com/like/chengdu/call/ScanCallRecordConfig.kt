package com.like.chengdu.call

/**
 * 扫描通话录音文件的配置(由后台配置)
 */
data class ScanCallRecordConfig(
    val filePath: String,// 通话录音文件路径。例如："/Music/Recordings/Call Recordings/"
    val scanType: Int,// 扫描类型。0：姓名；1：创建时间；2：修改时间；3：姓名+创建时间；4：姓名+修改时间
    val fileNameTimeFormat: String,// 如果文件名包含时间，需要指定这个时间格式
    val fileNameSeparator: String,// 如果文件名是组合方式（scanType = 3、4），需要指定这个分隔符
    val fileSuffix: String,// 通话录音文件后缀
    val scanTimeError: Int,// 扫描时间误差值。秒
)