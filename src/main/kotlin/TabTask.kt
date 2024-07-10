package burpee

import burp.api.montoya.MontoyaApi
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.*

val ignoreHeaderNames = mutableListOf<String>()
val parseScope = mutableListOf("Outline","Path","Headers", "Params", "Cookies")
val valueDecode = mutableListOf<String>()
class TabTask(private val api: MontoyaApi) : JPanel() {
    init {
        this.layout = GridBagLayout()
        val initialIgnoreParamHeaderNames = listOf(
            "Host",
            "User-Agent",
            "Accept",
            "Accept-Language",
            "Accept-Encoding",
            "Referer",
            "Origin",
            "Sec-Fetch-Dest",
            "Sec-Fetch-Mode",
            "Sec-Fetch-Site",
            "Priority",
            "Pragma",
            "Cache-Control",
            "Content-Length",
            "Te",
            "Connection",)
        ignoreHeaderNames.addAll(initialIgnoreParamHeaderNames)
        addIgnoreBlock()
        addCheckBoxes()
        addDecodeCheck()
    }

    private fun addIgnoreBlock() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)
        //ブロックタイトル
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 0
        c.gridy = 0
        c.gridwidth = 2
        this.add(JLabel("Ignore Header"), c)
        // ignoreリスト表示
        //初期値
        val listModel = DefaultListModel<String>()
        ignoreHeaderNames.forEach { listModel.addElement(it) }
        val jList = JList(listModel)
        jList.visibleRowCount = 10
        jList.fixedCellWidth = 100
        val scrollPane = JScrollPane(jList)
        c.fill = GridBagConstraints.BOTH
        c.gridx = 0
        c.gridy = 1
        c.gridheight = 5
        c.gridwidth = 2
        this.add(scrollPane, c)

        // Add Pasteボタン
        val addPasteButton = JButton("Add Paste")
        addPasteButton.toolTipText =
            "Add headers that are excluded from parsing from the clipboard."
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 0
        c.gridy = 14
        c.gridwidth = 1
        this.add(addPasteButton, c)

        // Overwrite Pasteボタン
        val overwritePasteButton = JButton("Overwrite Paste")
        overwritePasteButton.toolTipText =
            "Overwrite headers that are excluded from parsing from the clipboard."
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 1
        c.gridy = 14
        c.gridwidth = 1
        this.add(overwritePasteButton, c)

        addPasteButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val contents: Transferable? = clipboard.getContents(null)
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    val pasteText = contents.getTransferData(DataFlavor.stringFlavor) as String
                    val items = pasteText.split("\n").filter { it.isNotEmpty() }
                    for (item in items) {
                        listModel.addElement(item)
                        ignoreHeaderNames.add(item)
                    }
                    api.logging().logToOutput("\r\nignoreHeaderNames:\r\n${ignoreHeaderNames}")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        //paste action
        overwritePasteButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val contents: Transferable? = clipboard.getContents(null)
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    val pasteText = contents.getTransferData(DataFlavor.stringFlavor) as String
                    val items = pasteText.split("\n").filter { it.isNotEmpty() }
                    listModel.clear()
                    ignoreHeaderNames.clear()
                    for (item in items) {
                        listModel.addElement(item)
                        ignoreHeaderNames.add(item)
                    }
                    api.logging().logToOutput("\r\nignoreHeaderNames:\r\n${ignoreHeaderNames}")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
    private fun addCheckBoxes() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)
        val outlineCheckBox = JCheckBox("Outline", true)
        val pathCheckBox = JCheckBox("Path", true)
        val headerCheckBox = JCheckBox("Headers", true)
        val paramsCheckBox = JCheckBox("Params", true)
        val cookiesCheckBox = JCheckBox("Cookies", true)

        outlineCheckBox.addActionListener {
            if (outlineCheckBox.isSelected) {
                parseScope.add("Outline")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            } else {
                parseScope.remove("Outline")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            }
        }

        pathCheckBox.addActionListener {
            if (pathCheckBox.isSelected) {
                parseScope.add("Path")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            } else {
                parseScope.remove("Path")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            }
        }

        headerCheckBox.addActionListener {
            if (headerCheckBox.isSelected) {
                parseScope.add("Headers")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            } else {
                parseScope.remove("Headers")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            }
        }

        cookiesCheckBox.addActionListener {
            if (cookiesCheckBox.isSelected) {
                parseScope.add("Cookies")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            } else {
                parseScope.remove("Cookies")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            }
        }

        paramsCheckBox.addActionListener {
            if (paramsCheckBox.isSelected) {
                parseScope.add("Params")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            } else {
                parseScope.remove("Params")
                api.logging().logToOutput("\r\nparseScope:\r\n${parseScope}")
            }
        }


        // 配置
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 2
        c.gridy = 0
        this.add(JLabel("Parse Scope"), c)
        c.gridy = 1
        this.add(outlineCheckBox, c)

        c.gridy = 2
        this.add(pathCheckBox, c)

        c.gridy = 3
        this.add(headerCheckBox, c)

        c.gridy = 4
        this.add(cookiesCheckBox, c)

        c.gridy = 5
        this.add(paramsCheckBox, c)


    }

    private fun addDecodeCheck() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)

        val urlDecCheckBox = JCheckBox("URL Decode", false)

        urlDecCheckBox.addActionListener {
            if (urlDecCheckBox.isSelected) {
                valueDecode.add("URL")
                api.logging().logToOutput("\r\nvalueDecode:\r\n${valueDecode}")
            } else {
                valueDecode.remove("URL")
                api.logging().logToOutput("\r\nvalueDecode:\r\n${valueDecode}")
            }
        }

        // 配置
        c.gridx = 3
        c.gridy = 0
        this.add(JLabel("Value Decoded"), c)
        c.gridy = 1
        this.add(urlDecCheckBox, c)

    }
    private fun addSpaceBlock(column: Int,height: Int) {
        val c = GridBagConstraints()
        c.gridx = column
        c.gridy = 0
        c.gridheight = height
        c.fill = GridBagConstraints.BOTH
        this.add(JSeparator(), c)
    }
}