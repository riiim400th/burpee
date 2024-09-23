package burpee

data class State(
    val mode: Int,
    val outputFile: String,
    val ignoreHeaderNames: List<String>,
    val parseScope: Map<String, Boolean>,
    val valueDecode: List<String>,
    val highlightRows: Boolean
)
