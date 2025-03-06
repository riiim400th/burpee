package burpee

object DefaultData {
    val requestSheetColumn = listOf(
        "No.",
        "Host",
        "Method",
        "Path",
        "Query",
        "URL",
        "Parameter count",
        "Status Code",
        "MimeType",
        "Extension",
        "Referer",
        "Notes"
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
            "Sec-Fetch-User",
            "Priority",
            "Pragma",
            "Cache-Control",
            "Content-Length",
            "Te",
            "Connection",
            "DNT",
            "Sec-Ch-Ua",
            "Sec-Ch-Ua-Platform",
            "If-Modified-Since",
            "If-None-Match",
            "Alt-Svc",
            "Timing-Allow-Origin",
            "Accept-Charset",
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
    const val REPO_ENDPOINT = "https://api.github.com/repos/riiim400th/burpee/releases/latest"
}