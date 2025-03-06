package burpee

import burp.api.montoya.core.Annotations
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameterType
import burp.api.montoya.http.message.params.ParsedHttpParameter
import burp.api.montoya.http.message.requests.HttpRequest


class TargetItem(val requestID: Int, requestResponse: HttpRequestResponse) {
    private val api = Api.api
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
            "Outline" to { outline() },
            "Path" to { paths() },
            "Params" to {
                param(urlParams) + param(bodyParams)
            },
            "Headers" to { header(state) },
            "Cookies" to { param(cookies) }
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

    private fun String.diff(value:String):String {
        return if(value == this) "" else this
    }

    private fun outline(): List<List<String>> {

        return listOf(
            listOf("Method", req.method()),
            listOf("Url", req.url()),
            listOf("Version", req.httpVersion()),
            listOf(),
            listOf("TYPE", "NAME", "VALUE", "VALUE(auto decoded)")
        )
    }

    private fun paths(): List<List<String>> {
        return paths.map { listOf("PATH", "-", it, Decode.autoDecode(it).diff(it)) }
    }

    private fun header(state: State): List<List<String>> {
        return headers
            .filter { it.name().lowercase() !in state.ignoreHeaderNames.map { it.lowercase() } }
            .map {
                listOf("Header", Decode.autoDecode(it.name()), it.value(), Decode.autoDecode(it.value()).diff(it.value()))
            }
    }

    private fun param(params: List<ParsedHttpParameter>): List<List<String>> {
        return params.map {
            val value = if (Escape.isUrlUnsafe(it.value())) Escape.removeNonPrintableChars(it.value()) else it.value()
            if (it.type() == HttpParameterType.MULTIPART_ATTRIBUTE) {
                listOf(it.type().toString(), it.name(), value,"")
            } else {
                listOf(it.type().toString(), Decode.decodeUrlEncoded(it.name()), value,Decode.autoDecode(value).diff(value))
            }

        }
    }

}