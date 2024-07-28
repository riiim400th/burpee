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
        api.logging().logToOutput("method:\t${method}")
        var url = request.url()
        if ("URL" in valueDecode) {
            url = URLDecoder.decode(url, "UTF-8")
    }
    api.logging().logToOutput("url:\t${url}")
        val httpVer = request.httpVersion()
    api.logging().logToOutput("httpVer:\t${httpVer}")

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
                .append("VALUE")
        }

    private fun providePathStrings(paths: List<String>, result: StringBuilder) {
        paths.forEachIndexed { i, path ->
            api.logging().logToOutput("path${i + 1}:\t${path}")
            var p = path
            if ("URL" in valueDecode) {
                p = URLDecoder.decode(path, "UTF-8")
            }
            result
                .append("\r\n")
                .append("PATH")
                .append("\t")
                .append("-")
                .append("\t")
                .append(p)
        }
    }
    private fun provideHeaderStrings(headers: List<HttpHeader>, result: StringBuilder) {
            headers.forEachIndexed { i, header ->
                api.logging().logToOutput("header${i + 1}:\t${header}")
                if (header.name() !in ignoreHeaderNames) {
                    var v = header.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), "")
                    if ("URL" in valueDecode) {
                        v = URLDecoder.decode(v, "UTF-8")
                    }
                    result
                        .append("\r\n")
                        .append("HEADER")
                        .append("\t")
                        .append(header.name())
                        .append("\t")
                        .append(v)
                } else {
                    api.logging().logToOutput("skip parse ${i + 1}:\t${header.name()} is in ignoreParamHeaderNames")
                }
            }
    }
    private fun provideParamsStrings(params: List<HttpParameter>, result: StringBuilder) {
        params.forEachIndexed { i, param ->
            api.logging().logToOutput("param${i + 1}:\t${param}")

            var v = param.value().replace(Regex("[\\x00-\\x1F\\x7F]+"), "")
            if ("URL" in valueDecode && !(param.type() == BODY && param.name() == "file")) {
                v = URLDecoder.decode(v, "UTF-8")
            }
            result
                .append("\r\n")
                .append(param.type())
                .append("\t")
                .append(param.name())
                .append("\t")
                .append(v)
        }
    }
    private fun provideSummaryRowStrings(request:  HttpRequest,result: StringBuilder,statusCode:String,note:String) {
        var url = request.url()
        var referer = request.headers().find { it.name() == "Referer" }?.value() ?: "?"
        if ("URL" in valueDecode) {
            url = URLDecoder.decode(url, "UTF-8")
            referer = URLDecoder.decode(referer, "UTF-8")
        }
        val urlNoParam = url.substringBefore("?")

        result
            .append("${requestID.toString()}\t")
            .append("\t")
            .append("$referer\t")
            .append("$urlNoParam\t")
            .append("$url\t")
            .append("${request.method()}\t")
            .append("$statusCode\t")
            .append("${request.parameters().count { it.type() != COOKIE }.toString()}\t")
            .append(note.replace("\t","    "))
    }


    private fun tsvToList(text: String): List<List<String>> {
        val lines = text.split("\r\n")
        val data = lines.map { line ->
            line.split("\t")
        }
        return data
    }

    override fun provideMenuItems(event: ContextMenuEvent): List<Component>? {
        if (event.isFromTool(ToolType.PROXY,ToolType.REPEATER, ToolType.TARGET, ToolType.LOGGER)) {
            val menuItemList: MutableList<Component> = mutableListOf()
            val copyParse = JMenuItem("burpee")
            val requestResponses: List<HttpRequestResponse> = if (event.messageEditorRequestResponse().isPresent) {
                listOf( event.messageEditorRequestResponse().get().requestResponse())
            } else {
                event.selectedRequestResponses().reversed()
            }
            requestResponses.forEach { requestResponse ->

                copyParse.addActionListener {
                    val result = StringBuilder()
                    val req = requestResponse.request()
                    val res = requestResponse.response()
                    val resStatusCode = res?.statusCode()?.toString() ?: ""
                    val annotation = requestResponse.annotations()
                    val notes = annotation.notes()
                    val highlight = annotation.highlightColor().name
                    api.logging().logToOutput("\r\nnotes to $notes")
                    api.logging().logToOutput("\r\nhighlight to $highlight")

                    val paths = requestResponse.request().path()
                        .split("/")
                        .map { it.substringBefore("?") }
                        .filter { it.isNotEmpty() }
                    val urlParams = req.parameters().filter { it.type() == URL }
                    val headers = req.headers()
                    val cookies = req.parameters().filter { it.type() == COOKIE }
                    val bodyParams = req.parameters().filter { it.type() != COOKIE && it.type() != URL }

                    if ("Outline" in parseScope) {
                        provideOutlineStrings(req, result)
                    }
                    if ("Path" in parseScope) {
                        providePathStrings(paths, result)
                    }
                    if ("Params" in parseScope) {
                        provideParamsStrings(urlParams, result)
                    }
                    if ("Headers" in parseScope) {
                        provideHeaderStrings(headers, result)
                    }
                    if ("Cookies" in parseScope) {
                        provideParamsStrings(cookies, result)
                    }
                    if ("Params" in parseScope) {
                        provideParamsStrings(bodyParams, result)
                    }
                    if (result.toString() == "") {
                        result.append("-\t-\t-")
                    }

                    val text = result.toString()

                    if (mode == 0 || mode == 2) {
                        val selection = StringSelection(text)
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(selection, selection)
                        api.logging().logToOutput("\r\ncopied:\t${text}")
                    }

                    if (mode == 1 || mode == 2) {
                        val excelTask = ExcelTask(api)
                        api.logging().logToOutput("requestID is${requestID}")

                        // insert summary detail
                        val summary = StringBuilder()
                        provideSummaryRowStrings(req, summary, resStatusCode,notes)
                        val summaryData = tsvToList(summary.toString())
                        excelTask.selectSheet("requests")
                        excelTask.insert(summaryData,false,highlight)
                        api.logging().logToOutput("requestID${requestID}:\tsummaryData:${summaryData}")

                        // insert request
                        val highlightRowsBool = highlightRows
                        highlightRows = false
                        val detailData = tsvToList(text)
                        excelTask.selectSheet("$requestID")
                        excelTask.insert(detailData)
                        highlightRows=highlightRowsBool
                        api.logging().logToOutput("requestID${requestID}:\tdetailData:${detailData}")

                        // save
                        excelTask.saveWorkbook()
                        api.logging().logToOutput("\r\nsaved to $outputFile")
                        ++requestID

                    }
                }
            }

            menuItemList.add(copyParse)

            return menuItemList
        }

        return null
    }
}