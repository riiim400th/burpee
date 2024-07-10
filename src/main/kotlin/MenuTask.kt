package burpee

import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameter
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import burp.api.montoya.http.message.params.HttpParameterType.*
import burp.api.montoya.http.message.requests.HttpRequest
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URLDecoder
import javax.swing.JMenuItem

class MenuTask(private val api: MontoyaApi) : ContextMenuItemsProvider {
    private fun provideOutlineStrings(request: HttpRequest, result: StringBuilder) {
        val method = request.method()
        api.logging().logToOutput("method:\r\n${method}")
        var url = request.url()
        if ("URL" in valueDecode) {
            url = URLDecoder.decode(url, "UTF-8")
    }
    api.logging().logToOutput("url:\r\n${url}")
        val httpVer = request.httpVersion()
    api.logging().logToOutput("httpVer:\r\n${httpVer}")

            result
                .append("Method\t")
                .append(method)
                .append("\r\n")
                .append("Url\t")
                .append(url)
                .append("\r\n")
                .append("Version\t")
                .append(httpVer)
                .append("\r\n")
                .append("\r\n")
                .append("TYPE\t")
                .append("NAME\t")
                .append("VALUE\t")
                .append("\r\n")
        }

    private fun providePathStrings(paths: List<String>, result: StringBuilder) {
        paths.forEachIndexed { i, path ->
            api.logging().logToOutput("path${i + 1}:\r\n${path}")
            var p = path
            if ("URL" in valueDecode) {
                p = URLDecoder.decode(path, "UTF-8")
            }
            result
                .append("PATH")
                .append("\t")
                .append("-")
                .append("\t")
                .append(p)
                .append("\r\n")
        }
    }
    private fun provideHeaderStrings(headers: List<HttpHeader>, result: StringBuilder) {
            headers.forEachIndexed { i, header ->
                api.logging().logToOutput("header${i + 1}:\r\n${header}")
                if (header.name() !in ignoreHeaderNames) {
                    var v = header.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), "")
                    if ("URL" in valueDecode) {
                        v = URLDecoder.decode(v, "UTF-8")
                    }
                    result
                        .append("HEADER")
                        .append("\t")
                        .append(header.name())
                        .append("\t")
                        .append(v)
                        .append("\r\n")
                } else {
                    api.logging().logToOutput("skip parse ${i + 1}:\r\n${header.name()} is in ignoreParamHeaderNames")
                }
            }
    }
    private fun provideParamsStrings(params: List<HttpParameter>, result: StringBuilder) {
        params.forEachIndexed { i, param ->
            api.logging().logToOutput("param${i + 1}:\r\n${param}")

            var v = param.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), "")
            if ("URL" in valueDecode && !(param.type() == BODY && param.name() == "file")) {
                v = URLDecoder.decode(v, "UTF-8")
            }
            result
                .append(param.type())
                .append("\t")
                .append(param.name())
                .append("\t")
                .append(v)
                .append("\r\n")
        }
    }

    override fun provideMenuItems(event: ContextMenuEvent): List<Component>? {
        if (event.isFromTool(ToolType.PROXY,ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            val menuItemList: MutableList<Component> = mutableListOf()
            val retrieveRequestItem = JMenuItem("Copy")
            val requestResponse: HttpRequestResponse = if (event.messageEditorRequestResponse().isPresent) {
                event.messageEditorRequestResponse().get().requestResponse()
            } else {
                event.selectedRequestResponses()[0]
            }

            retrieveRequestItem.addActionListener {
                val result = StringBuilder()
                val req = requestResponse.request()
                val paths = requestResponse.request().path()
                    .split("/")
                    .map { it.substringBefore("?") }
                    .filter { it.isNotEmpty() }
                val urlParams = req.parameters().filter { it.type() == URL }
                val headers = req.headers()
                val cookies = req.parameters().filter { it.type() == COOKIE }
                val bodyParams = req.parameters().filter { it.type() != COOKIE && it.type() != URL }

                if ("Outline" in parseScope) {
                    provideOutlineStrings(req,result)
                }
                if ("Path" in parseScope) {
                    providePathStrings(paths,result)
                }
                if ("Params" in parseScope) {
                    provideParamsStrings(urlParams,result)
                }
                if ("Headers" in parseScope) {
                    provideHeaderStrings(headers,result)
                }
                if ("Cookies" in parseScope) {
                    provideParamsStrings(cookies,result)
                }
                if ("Params" in parseScope) {
                    provideParamsStrings(bodyParams,result)
                }
                if (result.toString() == ""){
                    result.append("-\t-\t-")
                }

                val text = result.toString()

                val selection = StringSelection(text)
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(selection, selection)
                api.logging().logToOutput("\r\ncopied:\r\n${text}")

            }
            menuItemList.add(retrieveRequestItem)

            return menuItemList
        }

        return null
    }
}