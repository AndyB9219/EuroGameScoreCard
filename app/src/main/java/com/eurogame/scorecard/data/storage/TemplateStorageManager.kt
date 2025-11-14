package com.eurogame.scorecard.data.storage

import android.content.Context
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import com.eurogame.scorecard.data.xml.ScorecardXmlParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TemplateStorageManager(private val context: Context) {

    private val templatesDir: File
        get() = File(context.filesDir, "templates").apply {
            if (!exists()) mkdirs()
        }

    fun saveTemplate(template: ScorecardTemplate, fileName: String): Boolean {
        return try {
            val file = File(templatesDir, "$fileName.xml")
            FileOutputStream(file).use { outputStream ->
                ScorecardXmlParser.serialize(template, outputStream)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadTemplate(fileName: String): ScorecardTemplate? {
        return try {
            val file = File(templatesDir, "$fileName.xml")
            if (!file.exists()) return null

            FileInputStream(file).use { inputStream ->
                ScorecardXmlParser.parse(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteTemplate(fileName: String): Boolean {
        return try {
            val file = File(templatesDir, "$fileName.xml")
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllTemplates(): List<TemplateInfo> {
        return try {
            templatesDir.listFiles { _, name -> name.endsWith(".xml") }
                ?.mapNotNull { file ->
                    loadTemplate(file.nameWithoutExtension)?.let { template ->
                        TemplateInfo(
                            fileName = file.nameWithoutExtension,
                            gameName = template.game.name,
                            subtitle = template.game.subtitle,
                            categoryCount = template.categories.size,
                            lastModified = file.lastModified()
                        )
                    }
                } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun templateExists(fileName: String): Boolean {
        val file = File(templatesDir, "$fileName.xml")
        return file.exists()
    }

    fun generateUniqueFileName(baseName: String): String {
        val sanitized = baseName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        var fileName = sanitized
        var counter = 1

        while (templateExists(fileName)) {
            fileName = "${sanitized}_$counter"
            counter++
        }

        return fileName
    }
}

data class TemplateInfo(
    val fileName: String,
    val gameName: String,
    val subtitle: String?,
    val categoryCount: Int,
    val lastModified: Long
)
