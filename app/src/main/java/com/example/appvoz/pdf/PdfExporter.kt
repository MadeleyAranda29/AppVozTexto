package com.example.appvoz.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PdfExporter contiene la lógica para generar y compartir un PDF a partir de un texto.
 * Mantiene la misma lógica que el proyecto original.
 */
object PdfExporter {
    fun saveTranscriptAsPdf(context: Context, packageName: String, text: String) {
        if (text.isBlank()) {
            Toast.makeText(context, "No hay transcripción para guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (docsDir == null) {
            Toast.makeText(context, "No se puede acceder al directorio de documentos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!docsDir.exists()) docsDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "transcripcion_$timestamp.pdf"
        val file = File(docsDir, filename)

        val pageWidth = 595
        val pageHeight = 842
        val document = PdfDocument()
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f * context.resources.displayMetrics.density
        }

        val x = 20f
        val yStart = 40f
        var y = yStart
        val maxWidth = pageWidth - 40
        val words = text.split(Regex("\\s+"))
        var line = StringBuilder()

        var pageNumber = 1
        var page: PdfDocument.Page = createPage(document, pageWidth, pageHeight, pageNumber)
        var canvas = page.canvas

        fun finishCurrentPageWithFooter(p: PdfDocument.Page) {
            val footerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f * context.resources.displayMetrics.density
            }
            val footerX = 20f
            var footerY = (pageHeight - 50).toFloat()
            canvas.drawText("UNIVERSIDAD NACIONAL DE PIURA", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("2025-ALBURQUEQUE ANTON SEGIO", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("ARANDA ZAPATA MADELEY", footerX, footerY, footerPaint)
            footerY += footerPaint.textSize + 4f
            canvas.drawText("MORAN PALACIOS NICK", footerX, footerY, footerPaint)

            document.finishPage(p)
        }

        for (w in words) {
            val testLine = if (line.isEmpty()) w else line.toString() + " " + w
            val textWidth = paint.measureText(testLine)
            if (textWidth > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                y += paint.textSize + 6f
                line = StringBuilder(w)
            } else {
                if (line.isEmpty()) line.append(w) else line.append(" ").append(w)
            }

            if (y > pageHeight - 120) {
                if (line.isNotEmpty()) {
                    canvas.drawText(line.toString(), x, y, paint)
                    line = StringBuilder()
                }
                finishCurrentPageWithFooter(page)
                pageNumber += 1
                page = createPage(document, pageWidth, pageHeight, pageNumber)
                canvas = page.canvas
                y = yStart
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, y, paint)
        }
        finishCurrentPageWithFooter(page)

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri: Uri = FileProvider.getUriForFile(context, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))

            Toast.makeText(context, "PDF guardado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Error al crear el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createPage(document: PdfDocument, width: Int, height: Int, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber).create()
        return document.startPage(pageInfo)
    }
}

