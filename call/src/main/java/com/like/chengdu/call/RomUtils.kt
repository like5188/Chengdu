package com.like.chengdu.call

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.*
import java.util.*

object RomUtils {
    private val ROM_HUAWEI = arrayOf("huawei")
    private val ROM_VIVO = arrayOf("vivo")
    private val ROM_XIAOMI = arrayOf("xiaomi")
    private val ROM_OPPO = arrayOf("oppo")
    private val ROM_LEECO = arrayOf("leeco", "letv")
    private val ROM_360 = arrayOf("360", "qiku")
    private val ROM_ZTE = arrayOf("zte")
    private val ROM_ONEPLUS = arrayOf("oneplus")
    private val ROM_NUBIA = arrayOf("nubia")
    private val ROM_COOLPAD = arrayOf("coolpad", "yulong")
    private val ROM_LG = arrayOf("lg", "lge")
    private val ROM_GOOGLE = arrayOf("google")
    private val ROM_SAMSUNG = arrayOf("samsung")
    private val ROM_MEIZU = arrayOf("meizu")
    private val ROM_LENOVO = arrayOf("lenovo")
    private val ROM_SMARTISAN = arrayOf("smartisan")
    private val ROM_HTC = arrayOf("htc")
    private val ROM_SONY = arrayOf("sony")
    private val ROM_GIONEE = arrayOf("gionee", "amigo")
    private val ROM_MOTOROLA = arrayOf("motorola")
    private const val VERSION_PROPERTY_HUAWEI = "ro.build.version.emui"
    private const val VERSION_PROPERTY_VIVO = "ro.vivo.os.build.display.id"
    private const val VERSION_PROPERTY_XIAOMI = "ro.build.version.incremental"
    private const val VERSION_PROPERTY_OPPO = "ro.build.version.opporom"
    private const val VERSION_PROPERTY_LEECO = "ro.letv.release.version"
    private const val VERSION_PROPERTY_360 = "ro.build.uiversion"
    private const val VERSION_PROPERTY_ZTE = "ro.build.MiFavor_version"
    private const val VERSION_PROPERTY_ONEPLUS = "ro.rom.version"
    private const val VERSION_PROPERTY_NUBIA = "ro.build.rom.id"
    private const val UNKNOWN = "unknown"
    private val manufacturer: String
        get() {
            val manufacturer = Build.MANUFACTURER
            if (!manufacturer.isNullOrEmpty()) {
                return manufacturer.lowercase(Locale.getDefault())
            }
            return UNKNOWN
        }
    private val brand: String
        get() {
            val brand = Build.BRAND
            if (!brand.isNullOrEmpty()) {
                return brand.lowercase(Locale.getDefault())
            }
            return UNKNOWN
        }
    private val romInfo: RomInfo by lazy {
        var name = ""
        var version = ""
        val brand = brand
        val manufacturer = manufacturer
        when {
            isRightRom(brand, manufacturer, *ROM_HUAWEI) -> {
                name = ROM_HUAWEI[0]
                version = getRomVersion(VERSION_PROPERTY_HUAWEI)
                val temp = version.split("_").toTypedArray()
                if (temp.size > 1) {
                    version = temp[1]
                }
            }
            isRightRom(brand, manufacturer, *ROM_VIVO) -> {
                name = ROM_VIVO[0]
                version = getRomVersion(VERSION_PROPERTY_VIVO)
            }
            isRightRom(brand, manufacturer, *ROM_XIAOMI) -> {
                name = ROM_XIAOMI[0]
                version = getRomVersion(VERSION_PROPERTY_XIAOMI)
            }
            isRightRom(brand, manufacturer, *ROM_OPPO) -> {
                name = ROM_OPPO[0]
                version = getRomVersion(VERSION_PROPERTY_OPPO)
            }
            isRightRom(brand, manufacturer, *ROM_LEECO) -> {
                name = ROM_LEECO[0]
                version = getRomVersion(VERSION_PROPERTY_LEECO)
            }
            isRightRom(brand, manufacturer, *ROM_360) -> {
                name = ROM_360[0]
                version = getRomVersion(VERSION_PROPERTY_360)
            }
            isRightRom(brand, manufacturer, *ROM_ZTE) -> {
                name = ROM_ZTE[0]
                version = getRomVersion(VERSION_PROPERTY_ZTE)
            }
            isRightRom(brand, manufacturer, *ROM_ONEPLUS) -> {
                name = ROM_ONEPLUS[0]
                version = getRomVersion(VERSION_PROPERTY_ONEPLUS)
            }
            isRightRom(brand, manufacturer, *ROM_NUBIA) -> {
                name = ROM_NUBIA[0]
                version = getRomVersion(VERSION_PROPERTY_NUBIA)
            }
            isRightRom(brand, manufacturer, *ROM_COOLPAD) -> {
                name = ROM_COOLPAD[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_LG) -> {
                name = ROM_LG[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_GOOGLE) -> {
                name = ROM_GOOGLE[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_SAMSUNG) -> {
                name = ROM_SAMSUNG[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_MEIZU) -> {
                name = ROM_MEIZU[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_LENOVO) -> {
                name = ROM_LENOVO[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_SMARTISAN) -> {
                name = ROM_SMARTISAN[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_HTC) -> {
                name = ROM_HTC[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_SONY) -> {
                name = ROM_SONY[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_GIONEE) -> {
                name = ROM_GIONEE[0]
                version = getRomVersion("")
            }
            isRightRom(brand, manufacturer, *ROM_MOTOROLA) -> {
                name = ROM_MOTOROLA[0]
                version = getRomVersion("")
            }
            else -> {
                name = manufacturer
                version = getRomVersion("")
            }
        }
        RomInfo(name, version)
    }

    val isHuawei: Boolean = ROM_HUAWEI[0] == romInfo.name
    val isVivo: Boolean = ROM_VIVO[0] == romInfo.name
    val isXiaomi: Boolean = ROM_XIAOMI[0] == romInfo.name
    val isOppo: Boolean = ROM_OPPO[0] == romInfo.name
    val isLeeco: Boolean = ROM_LEECO[0] == romInfo.name
    fun is360(): Boolean = ROM_360[0] == romInfo.name
    val isZte: Boolean = ROM_ZTE[0] == romInfo.name
    val isOneplus: Boolean = ROM_ONEPLUS[0] == romInfo.name
    val isNubia: Boolean = ROM_NUBIA[0] == romInfo.name
    val isCoolpad: Boolean = ROM_COOLPAD[0] == romInfo.name
    val isLg: Boolean = ROM_LG[0] == romInfo.name
    val isGoogle: Boolean = ROM_GOOGLE[0] == romInfo.name
    val isSamsung: Boolean = ROM_SAMSUNG[0] == romInfo.name
    val isMeizu: Boolean = ROM_MEIZU[0] == romInfo.name
    val isLenovo: Boolean = ROM_LENOVO[0] == romInfo.name
    val isSmartisan: Boolean = ROM_SMARTISAN[0] == romInfo.name
    val isHtc: Boolean = ROM_HTC[0] == romInfo.name
    val isSony: Boolean = ROM_SONY[0] == romInfo.name
    val isGionee: Boolean = ROM_GIONEE[0] == romInfo.name
    val isMotorola: Boolean = ROM_MOTOROLA[0] == romInfo.name

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) {
                return true
            }
        }
        return false
    }

    private fun getRomVersion(propertyName: String): String {
        var ret = ""
        if (!TextUtils.isEmpty(propertyName)) {
            ret = getSystemProperty(propertyName)
        }
        if (TextUtils.isEmpty(ret) || ret == UNKNOWN) {
            try {
                val display = Build.DISPLAY
                if (!TextUtils.isEmpty(display)) {
                    ret = display.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
        }
        return if (TextUtils.isEmpty(ret)) {
            UNKNOWN
        } else ret
    }

    private fun getSystemProperty(name: String): String {
        var prop = getSystemPropertyByShell(name)
        if (!TextUtils.isEmpty(prop)) return prop
        prop = getSystemPropertyByStream(name)
        if (!TextUtils.isEmpty(prop)) return prop
        return if (Build.VERSION.SDK_INT < 28) {
            getSystemPropertyByReflect(name)
        } else prop
    }

    private fun getSystemPropertyByShell(propName: String): String {
        var line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            val ret = input.readLine()
            if (ret != null) {
                return ret
            }
        } catch (ignore: IOException) {
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (ignore: IOException) { /**/
                }
            }
        }
        return ""
    }

    private fun getSystemPropertyByStream(key: String): String {
        try {
            val prop = Properties()
            val `is` = FileInputStream(
                File(Environment.getRootDirectory(), "build.prop")
            )
            prop.load(`is`)
            return prop.getProperty(key, "")
        } catch (ignore: Exception) { /**/
        }
        return ""
    }

    private fun getSystemPropertyByReflect(key: String): String {
        try {
            @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
            val getMethod = clz.getMethod("get", String::class.java, String::class.java)
            return getMethod.invoke(clz, key, "") as String
        } catch (e: Exception) { /**/
        }
        return ""
    }

}
