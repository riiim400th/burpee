package burpee

import burp.api.montoya.MontoyaApi
import burp.api.montoya.core.ToolType
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import burp.api.montoya.http.message.params.HttpParameterType.*
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JMenuItem

class MenuTask(private val api: MontoyaApi) : ContextMenuItemsProvider {

    override fun provideMenuItems(event: ContextMenuEvent): List<Component>? {
        if (event.isFromTool(ToolType.PROXY,ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            val menuItemList: MutableList<Component> = mutableListOf()

            val retrieveRequestItem = JMenuItem("copy parsed params")
            val retrieveCookieItem = JMenuItem("copy cookie params")
            val requestResponse: HttpRequestResponse = if (event.messageEditorRequestResponse().isPresent) {
                event.messageEditorRequestResponse().get().requestResponse()
            } else {
                event.selectedRequestResponses()[0]
            }

            retrieveRequestItem.addActionListener {
                val result = StringBuilder()
                val params = requestResponse.request().parameters()
                if (params.isEmpty()) {
                    result.append("-\t-\t-")
                } else {
                    params.forEachIndexed { i, param ->
                        if (param.type() == COOKIE) {
                            return@forEachIndexed
                        }
                        api.logging().logToOutput("param${i + 1}:\r\n${param}")
                        result
                            .append(param.name())
                            .append("\t")
                            .append(param.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), ""))
                            .append("\t")
                            .append(param.type())
                            .append("\r\n")
                    }
                }
                val text = result.toString()

                val selection = StringSelection(text)
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(selection, selection)
                api.logging().logToOutput("copied:\r\n${text}")

            }
            menuItemList.add(retrieveRequestItem)

            retrieveCookieItem.addActionListener {
                val result = StringBuilder()
                val params = requestResponse.request().parameters()
                if (params.isEmpty()) {
                    result.append("-\t-\t-")
                } else {
                    params.forEachIndexed { i, param ->
                        if (param.type() != COOKIE) {
                            return@forEachIndexed
                        }
                        api.logging().logToOutput("param${i + 1}:\r\n${param}")
                        result
                            .append(param.name())
                            .append("\t")
                            .append(param.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), ""))
                            .append("\t")
                            .append(param.type())
                            .append("\r\n")
                    }
                }
                val text = result.toString()

                val selection = StringSelection(text)
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(selection, selection)
                api.logging().logToOutput("copied:\r\n${text}\r\n")

            }

            menuItemList.add(retrieveCookieItem)

            return menuItemList
        }

        return null
    }
}