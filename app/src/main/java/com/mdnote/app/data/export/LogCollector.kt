package com.mdnote.app.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志收集工具类
 * 收集应用诊断信息用于问题排查
 */
class LogCollector(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 收集诊断日志并导出为文件
     */
    fun collectAndExportLogs(): Uri? {
        val logContent = buildLogContent()
        val fileName = "MdNote_DiagnosticLog_${dateFormat.format(Date())}.txt"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(fileName, "text/plain", logContent.toByteArray())
        } else {
            saveToExternalStorage(fileName, "text/plain", logContent.toByteArray())
        }
    }

    /**
     * 分享日志文件
     */
    fun shareLogs(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享诊断日志"))
    }

    private fun buildLogContent(): String {
        val sb = StringBuilder()

        sb.appendLine("=" .repeat(60))
        sb.appendLine("           MdNote 诊断日志")
        sb.appendLine("=".repeat(60))
        sb.appendLine("生成时间: ${formatDate(System.currentTimeMillis())}")
        sb.appendLine()

        // 应用信息
        sb.appendLine("--- 应用信息 ---")
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            sb.appendLine("应用名称: MdNote")
            sb.appendLine("包名: ${context.packageName}")
            sb.appendLine("版本名: ${packageInfo.versionName}")
            sb.appendLine("版本号: ${packageInfo.versionCode}")
            sb.appendLine("编译时间: ${formatDate(packageInfo.lastUpdateTime)}")
        } catch (e: Exception) {
            sb.appendLine("应用信息获取失败: ${e.message}")
        }
        sb.appendLine()

        // 设备信息
        sb.appendLine("--- 设备信息 ---")
        sb.appendLine("品牌: ${Build.BRAND}")
        sb.appendLine("型号: ${Build.MODEL}")
        sb.appendLine("设备: ${Build.DEVICE}")
        sb.appendLine("产品: ${Build.PRODUCT}")
        sb.appendLine("Android 版本: ${Build.VERSION.RELEASE}")
        sb.appendLine("API 级别: ${Build.VERSION.SDK_INT}")
        sb.appendLine("构建 ID: ${Build.DISPLAY}")
        sb.appendLine()

        // 系统信息
        sb.appendLine("--- 系统信息 ---")
        sb.appendLine("语言: ${Locale.getDefault().displayName}")
        sb.appendLine("时区: ${TimeZone.getDefault().id}")
        sb.appendLine("可用内存: ${getAvailableMemory()}")
        sb.appendLine("总内存: ${getTotalMemory()}")
        sb.appendLine("存储空间: ${getStorageInfo()}")
        sb.appendLine()

        // 数据库信息
        sb.appendLine("--- 数据库信息 ---")
        try {
            val dbFile = context.getDatabasePath("notes_database")
            if (dbFile.exists()) {
                sb.appendLine("数据库路径: ${dbFile.absolutePath}")
                sb.appendLine("数据库大小: ${formatFileSize(dbFile.length())}")
                val walFile = File(dbFile.absolutePath + "-wal")
                if (walFile.exists()) {
                    sb.appendLine("WAL 文件大小: ${formatFileSize(walFile.length())}")
                }
            } else {
                sb.appendLine("数据库文件不存在")
            }
        } catch (e: Exception) {
            sb.appendLine("数据库信息获取失败: ${e.message}")
        }
        sb.appendLine()

        // 应用崩溃日志
        sb.appendLine("--- 崩溃日志 ---")
        val crashLogContent = getCrashLogContent()
        if (crashLogContent.isNotEmpty()) {
            sb.appendLine(crashLogContent)
        } else {
            sb.appendLine("无崩溃记录")
        }
        sb.appendLine()

        sb.appendLine("=".repeat(60))
        sb.appendLine("日志结束")
        sb.appendLine("=".repeat(60))

        return sb.toString()
    }

    private fun getAvailableMemory(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return formatFileSize(usedMemory) + " / " + formatFileSize(runtime.maxMemory())
    }

    private fun getTotalMemory(): String {
        val runtime = Runtime.getRuntime()
        return formatFileSize(runtime.totalMemory())
    }

    private fun getStorageInfo(): String {
        try {
            val stat = android.os.StatFs(Environment.getDataDirectory().absolutePath)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            return "可用: ${formatFileSize(availableBytes)} / 总计: ${formatFileSize(totalBytes)}"
        } catch (e: Exception) {
            return "获取失败"
        }
    }

    private fun getCrashLogContent(): String {
        return try {
            val crashLogDir = File(context.filesDir, "crash_logs")
            if (crashLogDir.exists() && crashLogDir.isDirectory) {
                crashLogDir.listFiles()
                    ?.sortedByDescending { it.lastModified() }
                    ?.take(3)
                    ?.joinToString("\n\n") { file ->
                        "--- ${formatDate(file.lastModified())} ---\n${file.readText()}"
                    } ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            "无法读取崩溃日志: ${e.message}"
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun saveViaMediaStore(fileName: String, mimeType: String, data: ByteArray): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MdNote")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return null

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(data)
        }

        return uri
    }

    private fun saveToExternalStorage(fileName: String, mimeType: String, data: ByteArray): Uri? {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MdNote"
        )
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        FileOutputStream(file).use { it.write(data) }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}