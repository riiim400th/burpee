package burpee

import burp.api.montoya.utilities.json.JsonNode
import java.net.HttpURLConnection
import java.net.URI

data class Latest (
    val htmlUrl: String,
    val tagName: String
)

data class UpdateInfo(
    val updatable: Boolean,
    val latest: Latest
)

class PollUpdate(ver: String) {
    private val nowVer = ver
    private val url = DefaultData.reposApiUrl

    private fun createConnection(url: String): HttpURLConnection? =
        runCatching { URI(url).toURL().openConnection() as HttpURLConnection }.getOrNull()

    // Response to String
    private fun getResponse(connection: HttpURLConnection?): String =
        connection?.let {
            runCatching {
                it.requestMethod = "GET"
                it.inputStream.bufferedReader().use { reader -> reader.readText() }
            }.getOrDefault("")
        } ?: ""

    // Return Latest instance
    private fun getLatest(responseString: String): Latest =
        runCatching { val o = JsonNode.jsonNode(responseString).asObject()
            Latest(o.get("html_url").asString(),o.get("tag_name").asString())}.getOrDefault(Latest("", nowVer))

    // return UpdateInfo instance
    private fun shouldUpdate(latest: Latest): UpdateInfo =
        UpdateInfo(latest.tagName != nowVer, latest)

    // PollUpdate main method
    fun poll(): UpdateInfo =
        createConnection(url)
            .let(::getResponse)
            .let(::getLatest)
            .let(::shouldUpdate)
}