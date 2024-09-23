package burpee

import burp.api.montoya.http.message.HttpRequestResponse
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JOptionPane

class OutPutTask(requestResponses: List<HttpRequestResponse>) {
    private val state = stateHolder.state
    val api = Api.api
    private val excelTask =
        ExcelTask(state).apply { if (getRequestID() == 0) insertRequestsSheetColumn().saveWorkbook() }
    private val firstRequestId = excelTask.getRequestID()
    private val targetItems = requestResponses.mapIndexed() { i, it -> TargetItem(firstRequestId + i, it) }

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

    fun outToExcel() {
        runCatching {
            when (state.highlightRows) {
                true -> targetItems.forEach {
                    excelTask.insert(it.summary(), false, it.annotation.highlightColor().name).saveWorkbook()
                }

                false -> excelTask.insert(targetItems.flatMap { it.summary() })
            }
            targetItems.forEach { targetItem ->
                excelTask.selectSheet("${targetItem.requestID}")
                excelTask.insert(targetItem.detail(state))
            }
            excelTask.saveWorkbook()
            Api.log("\r\nSuccess to output ${targetItems.size} items to ${state.outputFile}")
        }.onFailure { e ->
            JOptionPane.showMessageDialog(
                null,
                "File not found. Did you delete it or leave it open?",
                "burpee",
                JOptionPane.INFORMATION_MESSAGE
            )

        }
    }
}