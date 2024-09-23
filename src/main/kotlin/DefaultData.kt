package burpee

object DefaultData {
    val requestSheetColumn = listOf(
        "No.",
        "Action",
        "Referer URL",
        "Dst URL",
        "Dst URL(with parameters)",
        "Method",
        "MimeType",
        "Status Code",
        "Parameter count",
        "Note"
    )
    val defaultState = State(
        mode = 0,
        outputFile = "",
        ignoreHeaderNames = listOf(
            "Host",
            "User-Agent",
            "Accept",
            "Accept-Language",
            "Accept-Encoding",
            "Referer",
            "Origin",
            "Sec-Fetch-Dest",
            "Sec-Fetch-Mode",
            "Sec-Fetch-Site",
            "Priority",
            "Pragma",
            "Cache-Control",
            "Content-Length",
            "Te",
            "Connection"
        ),
        highlightRows = false,
        parseScope = mapOf(
            "Outline" to true,
            "Path" to true,
            "Headers" to true,
            "Params" to true,
            "Cookies" to true
        ),
        valueDecode = listOf("URL")
    )
}