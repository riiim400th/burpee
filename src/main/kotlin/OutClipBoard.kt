package burpee

import burp.api.montoya.http.message.HttpRequestResponse
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class OutClipBoard(requestResponses: List<HttpRequestResponse>) {
    private val state = stateHolder.state
    val api = Api.api
    private val targetItems = requestResponses.mapIndexed() { i, it -> TargetItem(0 + i, it) }
    fun outToClipBoard() {
        targetItems.forEach { targetItem ->
            val text = targetItem.detail(state).joinToString(separator = "\r\n") { row ->
                row.joinToString(separator = "\t")
            }
            val selection = StringSelection(text)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(selection, selection)
            Api.log("\r\ncopied:\t${text}")
        }
    }
}