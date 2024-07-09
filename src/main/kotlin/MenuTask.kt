package burpee

import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.message.HttpHeader
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.params.HttpParameter
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import burp.api.montoya.http.message.params.HttpParameterType.*
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URLDecoder
import javax.swing.JMenuItem

class MenuTask(private val api: MontoyaApi) : ContextMenuItemsProvider {
    private fun provideHeaderStrings(headers: List<HttpHeader>, result: StringBuilder) {
            headers.forEachIndexed { i, header ->
                api.logging().logToOutput("header${i + 1}:\r\n${header}")
                if (header.name() !in ignoreHeaderNames) {
                    var v = header.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), "")
                    if ("URL" in valueDecode) {
                        v = URLDecoder.decode(v, "UTF-8")
                    }
                    result
                        .append(header.name())
                        .append("\t")
                        .append(v)
                        .append("\t")
                        .append("HEADER")
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
                .append(param.name())
                .append("\t")
                .append(v)
                .append("\t")
                .append(param.type())
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
                val headers = requestResponse.request().headers()
                val params = requestResponse.request().parameters().filter { it.type() != COOKIE }
                val cookies = requestResponse.request().parameters().filter { it.type() == COOKIE }
                if ("Headers" in parseScope) {

                    provideHeaderStrings(headers,result)
                }
                if ("Params" in parseScope) {
                    provideParamsStrings(params,result)
                }
                if ("Cookies" in parseScope) {
                    provideParamsStrings(cookies,result)
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