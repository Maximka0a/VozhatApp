package com.example.vozhatapp.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.vozhatapp.presentation.analytics.ExportFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Service for exporting reports in various formats
 */
class ReportExportService @Inject constructor(
    private val context: Context
) {
    /**
     * Export analytics data to the specified format and share the file
     */
    fun exportReport(
        title: String,
        data: Map<String, Any>,
        format: ExportFormat,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // Create a file in the app's external files directory
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())

            val extension = when (format) {
                ExportFormat.PDF -> "pdf"
                ExportFormat.EXCEL -> "xlsx"
                ExportFormat.CSV -> "csv"
            }

            val fileName = "${title.replace(" ", "_")}_${timestamp}.$extension"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            // Generate file content based on format
            when (format) {
                ExportFormat.PDF -> generatePdfReport(file, title, data)
                ExportFormat.EXCEL -> generateExcelReport(file, title, data)
                ExportFormat.CSV -> generateCsvReport(file, title, data)
            }

            // Get content URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Share the file
            onSuccess(uri)

        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Share the exported file
     */
    fun shareFile(uri: Uri, format: ExportFormat) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (format) {
                ExportFormat.PDF -> "application/pdf"
                ExportFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ExportFormat.CSV -> "text/csv"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Поделиться отчетом")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // In a real implementation, you would use libraries like:
    // - iText for PDF generation
    // - Apache POI for Excel generation
    // - OpenCSV for CSV generation

    private fun generatePdfReport(file: File, title: String, data: Map<String, Any>) {
        // Placeholder for PDF generation logic
        file.writeText("PDF Report: $title\n\n" + formatDataAsText(data))
    }

    private fun generateExcelReport(file: File, title: String, data: Map<String, Any>) {
        // Placeholder for Excel generation logic
        file.writeText("Excel Report: $title\n\n" + formatDataAsText(data))
    }

    private fun generateCsvReport(file: File, title: String, data: Map<String, Any>) {
        // Placeholder for CSV generation logic
        val content = StringBuilder()
        content.append("Category,Value\n")

        data.forEach { (key, value) ->
            content.append("\"$key\",\"$value\"\n")
        }

        file.writeText(content.toString())
    }

    private fun formatDataAsText(data: Map<String, Any>): String {
        val content = StringBuilder()
        data.forEach { (key, value) ->
            content.append("$key: $value\n")
        }
        return content.toString()
    }
}