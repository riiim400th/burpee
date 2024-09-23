package burpee

import java.io.File

object Validation {
    fun validState(state: State): List<String> {
        val errors = mutableListOf<String>()
        checkFile(state)?.let { errors.add(it) }
        checkMode(state)?.let { errors.add(it) }
        checkHighlightValid(state)?.let { errors.add(it) }
        return errors.toList()
    }

    private fun checkFile(state: State): String? {
        return when (state.outputFile != "" && !File(state.outputFile).exists()) {
            true -> "File not exist ${state.outputFile}"
            false -> null
        }
    }

    private fun modeIsValid(state: State): Boolean {
        return when (state.mode) {
            0 -> true
            1 -> state.outputFile != ""
            2 -> state.outputFile != ""
            else -> false
        }
    }

    private fun checkMode(state: State): String? {
        return when (modeIsValid(state)) {
            true -> null
            false -> "Select file at first."
        }
    }

    private fun checkHighlightValid(state: State): String? {
        return if (state.outputFile != "") {
            null
        } else {
            when (state.highlightRows) {
                true -> "Select file at first."
                false -> null
            }
        }
    }
}
