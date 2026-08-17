package com.maimonthlyhoppinings.data

import android.content.Context
import java.io.File
import java.util.UUID

data class FeedbackNote(
    val id: String,
    val markdown: String,
    val updatedAtMillis: Long,
) {
    val title: String
        get() {
            val line = markdown.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?.removePrefix("### ")
                ?.removePrefix("## ")
                ?.removePrefix("# ")
                ?.trim()
            return line?.ifEmpty { null } ?: "Untitled note"
        }
}

/** On-device markdown notes for Settings → Feedback. Not a journal backup. */
class FeedbackNotesStore(
    context: Context,
) {
    private val dir = File(context.filesDir, DIR_NAME)
    private val legacyFile = File(context.filesDir, LEGACY_FILE)

    fun list(): List<FeedbackNote> {
        migrateLegacy()
        if (!dir.exists()) return emptyList()
        return dir.listFiles { file -> file.extension == "md" }
            .orEmpty()
            .map { file ->
                FeedbackNote(
                    id = file.nameWithoutExtension,
                    markdown = file.readText(),
                    updatedAtMillis = file.lastModified(),
                )
            }
            .sortedByDescending { it.updatedAtMillis }
    }

    fun read(id: String): FeedbackNote? {
        val file = noteFile(id)
        if (!file.exists()) return null
        return FeedbackNote(
            id = id,
            markdown = file.readText(),
            updatedAtMillis = file.lastModified(),
        )
    }

    fun create(markdown: String = ""): FeedbackNote {
        dir.mkdirs()
        val id = UUID.randomUUID().toString()
        val file = noteFile(id)
        file.writeText(markdown)
        return FeedbackNote(id = id, markdown = markdown, updatedAtMillis = file.lastModified())
    }

    fun write(id: String, markdown: String): FeedbackNote {
        dir.mkdirs()
        val file = noteFile(id)
        file.writeText(markdown)
        return FeedbackNote(id = id, markdown = markdown, updatedAtMillis = file.lastModified())
    }

    fun delete(id: String) {
        noteFile(id).delete()
    }

    private fun noteFile(id: String): File = File(dir, "$id.md")

    private fun migrateLegacy() {
        if (!legacyFile.exists()) return
        val text = legacyFile.readText()
        if (text.isNotBlank()) {
            create(text)
        }
        legacyFile.delete()
    }

    companion object {
        const val DIR_NAME = "feedback-notes"
        const val LEGACY_FILE = "feedback-notes.md"
    }
}
