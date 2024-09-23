package burpee

import java.io.File

object SelectFile {
    private fun changeableMode(currentMode: Int): Int =
        listOf(currentMode, 1).maxOrNull() ?: 1

    fun processSelectedFile(file: File): String =
        listOf(
            validateFileExtension(file),
            ensureFileExists(file),
            updateState(file)
        ).firstOrNull { it.isNotEmpty() } ?: ""

    private fun validateFileExtension(file: File): String =
        when (file.extension) {
            "xlsx" -> ""
            else -> "File extension must be '.xlsx'"
        }

    private fun ensureFileExists(file: File): String =
        when {
            file.exists() -> ""
            else -> createExcelFile(file)
        }

    private fun createExcelFile(file: File): String =
        runCatching {
            file.createNewFile()
            Api.log("create file ${file.name}.")
            ""
        }.getOrElse { e -> "Error creating file: ${e.message}" }

    private fun updateState(file: File): String {
        stateHolder.state = stateHolder.state.copy(
            mode = changeableMode(stateHolder.state.mode),
            outputFile = file.absolutePath
        )
        return ""
    }


}