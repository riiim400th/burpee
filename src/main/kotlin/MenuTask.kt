package burpee

import burp.api.montoya.core.ToolType
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import java.awt.Component
import javax.swing.*

class MenuTask : ContextMenuItemsProvider {
    override fun provideMenuItems(event: ContextMenuEvent): List<Component>? {
        if (event.isFromTool(ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            val menuItemList: MutableList<Component> = mutableListOf()
            val burpeeRun = JMenuItem("burpee")
            val requestResponses: List<HttpRequestResponse> = when (event.messageEditorRequestResponse().isPresent) {
                true -> listOf(event.messageEditorRequestResponse().get().requestResponse())
                else -> event.selectedRequestResponses().reversed()
            }
            burpeeRun.addActionListener {
                val outPutTask = OutPutTask(requestResponses)
                when (stateHolder.state.mode) {
                    0 -> outPutTask.outToClipBoard()
                    1 -> outPutTask.outToExcel()
                    2 -> {
                        outPutTask.outToClipBoard()
                        outPutTask.outToExcel()
                    }
                }
            }
            menuItemList.add(burpeeRun)
            return menuItemList
        }

        return null
    }
}