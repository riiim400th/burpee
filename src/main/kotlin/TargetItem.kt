package burpee

import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.http.message.params.ParsedHttpParameter
import burp.api.montoya.http.message.requests.HttpRequest
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class TargetItem(val requestID: Int, requestResponse: HttpRequestResponse) {
    private val api = Api.api
    private val urlUtil = api.utilities().urlUtils()
    private val byteUtil = api.utilities().byteUtils()
    private val mimeType = requestResponse.response()?.mimeType()?.name?.takeIf { it != "UNRECOGNIZED" } ?: ""
    private val req: HttpRequest = requestResponse.request()
    private val statusCode = requestResponse.response()?.statusCode()?.toString() ?: ""
    val annotation: Annotations = requestResponse.annotations()
    private val paths by lazy {
        req.path().split("/")
            .map { it.substringBefore("?") }
            .filter { it.isNotEmpty() }
    }

    private val urlParams by lazy {
        req.parameters()
            .filter { it.type() == HttpParameterType.URL }
    }

    private val headers by lazy {
        req.headers()
    }

    private val cookies by lazy {
        req.parameters()
            .filter { it.type() == HttpParameterType.COOKIE }
    }

    private val bodyParams by lazy {
        req.parameters()
            .filter { it.type() !in listOf(HttpParameterType.COOKIE, HttpParameterType.URL) }
    }

    fun detail(state: State): List<List<String>> {
        return mapOf(
            "Outline" to { outline(state) },
            "Path" to { paths(state) },
            "Params" to {
                param(urlParams, state) + param(bodyParams, state)
            },
            "Headers" to { header(state) },
            "Cookies" to { param(cookies, state) }
        ).flatMap { (scope, function) ->
            if (state.parseScope[scope] == true) {
                function()
            } else {
                emptyList()
            }
        }
    }

    fun summary(): List<List<String>> {
        val referer = req.headers().find { it.name() == "Referer" }?.value() ?: " "
        val paramCount = req.parameters().count { it.type() != HttpParameterType.COOKIE }.toString()
        val outputNote = annotation.notes().replace("\t", "    ")


        return listOf(
            listOf(
                requestID.toString(),
                req.httpService().host(),
                req.method(),
                req.pathWithoutQuery(),
                req.query(),
                req.url(),
                paramCount,
                statusCode,
                mimeType,
                req.fileExtension(),
                referer,
                outputNote
            )
        )
    }

    private fun decodeIfNeeded(str: String, state: State): String {
        return when {
            "URL" in state.valueDecode -> try {
                urlUtil.decode(str)
            } catch (e: IllegalArgumentException) {
                Api.log("\r\nCaught IllegalArgumentException in function decodeIfNeeded\r\n")
                return str
            }
            else -> str
        }
    }

    private fun toStringBytes(byteString: String): String {
        val result:String
        try {
            result = byteUtil.convertToString(byteUtil.convertFromString(byteString))
        } catch (e:IllegalArgumentException) {
            Api.log("\r\nCaught IllegalArgumentException in function toStringBytes\r\n")
            return ""
        }
        return result
    }

    private fun outline(state: State): List<List<String>> {
        val outputUrl = decodeIfNeeded(req.url(), state)

        return listOf(
            listOf("Method", req.method()),
            listOf("Url", outputUrl),
            listOf("Version", req.httpVersion()),
            listOf(),
            listOf("TYPE", "NAME", "VALUE")
        )
    }

    private fun paths(state: State): List<List<String>> {
        return paths.map { listOf("PATH", "-", decodeIfNeeded(it, state)) }
    }

    private fun header(state: State): List<List<String>> {
        return headers
            .filter { it.name() !in state.ignoreHeaderNames }
            .map {
                listOf("Header", decodeIfNeeded(it.name(), state), decodeIfNeeded(it.value(), state))
            }
    }

    private fun param(params: List<ParsedHttpParameter>, state: State): List<List<String>> {
        return params.map {
            val outputValue = when {
                it.type() == HttpParameterType.MULTIPART_ATTRIBUTE -> toStringBytes(it.value())
                else -> it.value()
            }
            listOf(it.type().toString(), decodeIfNeeded(it.name(), state), decodeIfNeeded(outputValue, state))
        }
    }

}