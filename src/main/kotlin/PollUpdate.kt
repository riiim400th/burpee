package burpee

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.HttpURLConnection
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
data class Latest @JsonCreator constructor(
    @JsonProperty("html_url") val htmlUrl: String,
    @JsonProperty("tag_name") val tagName: String
)

data class UpdateInfo(
    val updatable: Boolean,
    val latest: Latest
)

class PollUpdate(ver: String) {
    private val nowVer = ver
    private val objectMapper = ObjectMapper()
    private val url = "https://api.github.com/repos/riiim400th/burpee/releases/latest"

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
        runCatching { objectMapper.readValue(responseString, Latest::class.java) }.getOrDefault(Latest("", ""))

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