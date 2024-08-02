package burpee

import burp.api.montoya.MontoyaApi
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

class PollUpdate(api: MontoyaApi, ver: String) {
    private val logging = api.logging()
    private val nowVer = ver
    private val objectMapper = ObjectMapper()

    private val url = "https://api.github.com/repos/riiim400th/burpee/releases/latest"

    private fun doGet(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun mapRes(responseString: String): Latest {
        return objectMapper.readValue(responseString, Latest::class.java)
    }

    fun poll(): UpdateInfo {
        val response = doGet(url)

        val latest = mapRes(response)
        val updateInfo = if (latest.tagName != nowVer) {
            UpdateInfo(true, Latest(latest.htmlUrl, latest.tagName))
        } else {
            UpdateInfo(false, Latest(latest.htmlUrl, latest.tagName))
        }
        return updateInfo
    }
}