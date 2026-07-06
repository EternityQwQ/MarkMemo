package com.mdnote.app.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.mdnote.app.data.model.Note
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 笔记导出工具类
 * 支持 Markdown 和 PDF 两种格式导出
 */
class ExportHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * 导出为 Markdown 文件
     */
    fun exportMarkdown(note: Note): Uri? {
        val fileName = sanitizeFileName(note.title) + "_" + dateFormat.format(Date()) + ".md"
        val content = buildMarkdownContent(note)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(fileName, "text/markdown", content.toByteArray())
        } else {
            saveToExternalStorage(fileName, "text/markdown", content.toByteArray())
        }
    }

    /**
     * 导出为 PDF 文件
     */
    fun exportPdf(note: Note): Uri? {
        val fileName = sanitizeFileName(note.title) + "_" + dateFormat.format(Date()) + ".pdf"
        val pdfBytes = generatePdf(note)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(fileName, "application/pdf", pdfBytes)
        } else {
            saveToExternalStorage(fileName, "application/pdf", pdfBytes)
        }
    }

    /**
     * 分享文件
     */
    fun shareFile(uri: Uri, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享笔记"))
    }

    private fun buildMarkdownContent(note: Note): String {
        val sb = StringBuilder()
        sb.appendLine("# ${note.title.ifEmpty { "无标题" }}")
        sb.appendLine()
        sb.appendLine("> 分类: ${note.category}")
        sb.appendLine("> 创建时间: ${formatDate(note.createdAt)}")
        sb.appendLine("> 更新时间: ${formatDate(note.updatedAt)}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine(note.content)
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        sb.appendLine("*由 MdNote 导出*")
        return sb.toString()
    }

    private fun generatePdf(note: Note): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 50f
        val maxWidth = pageInfo.pageWidth - 2 * margin
        var y = margin

        // Title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1C1B1F")
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val titleText = note.title.ifEmpty { "无标题" }
        canvas.drawText(titleText, margin, y + titlePaint.textSize, titlePaint)
        y += titlePaint.textSize + 30f

        // Metadata
        val metaPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#79747E")
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("分类: ${note.category}", margin, y, metaPaint)
        y += metaPaint.textSize + 6f
        canvas.drawText("创建: ${formatDate(note.createdAt)}", margin, y, metaPaint)
        y += metaPaint.textSize + 6f
        canvas.drawText("更新: ${formatDate(note.updatedAt)}", margin, y, metaPaint)
        y += metaPaint.textSize + 20f

        // Divider
        val dividerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y, pageInfo.pageWidth - margin, y, dividerPaint)
        y += 20f

        // Content
        val contentPaint = TextPaint().apply {
            color = android.graphics.Color.parseColor("#1C1B1F")
            textSize = 14f
            isAntiAlias = true
        }
        val content = note.content
        val staticLayout = StaticLayout.Builder.obtain(
            content, 0, content.length, contentPaint, maxWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1f)
            .build()

        canvas.save()
        canvas.translate(margin, y)
        staticLayout.draw(canvas)
        canvas.restore()

        document.finishPage(page)

        val outputStream = java.io.ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()

        return outputStream.toByteArray()
    }

    private fun sanitizeFileName(name: String): String {
        val base = name.ifEmpty { "note" }
        return base.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(50)
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