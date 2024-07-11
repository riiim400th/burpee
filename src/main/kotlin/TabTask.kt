package burpee

import burp.api.montoya.MontoyaApi
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.FileOutputStream
import javax.swing.*

val ignoreHeaderNames = mutableListOf<String>()
val parseScope = mutableListOf("Outline","Path","Headers", "Params", "Cookies")
val valueDecode = mutableListOf<String>()
var outputFile = ""
val comboBox = JComboBox(mode_options.values.toTypedArray())
var requestID = 0
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
        addModeCheck()
        addExcelFile()
        addSeparator(2,0)
        addIgnoreBlock()
        addCheckBoxes()
        addDecodeCheck()


    }

    private fun addModeCheck() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)

        comboBox.addActionListener {
            if (comboBox.selectedIndex==0){
                setMode(0,true)
                api.logging().logToOutput("Set mode:\t$mode")
                comboBox.selectedIndex = mode
                if (mode!=0){alert("Select file at first.")}
            }
            if (comboBox.selectedIndex==1){
                setMode(1,true)
                api.logging().logToOutput("Set mode:\t$mode")
                comboBox.selectedIndex = mode
                if (mode!=1){alert("Select file at first.")}
            }
            if (comboBox.selectedIndex==2) {
                setMode(2,true)
                api.logging().logToOutput("Set mode:\t$mode")
                comboBox.selectedIndex = mode
                if (mode!=2){alert("Select file at first.")}
            }

        }

        c.gridx = 0
        c.gridy =0
        this.add(JLabel("Output to: "), c)
        c.gridy = 1
        this.add(comboBox, c)

    }

    private fun addExcelFile() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)
        val filePathLabel = JLabel("No file selected")
        val fileButton = JButton("Select File")

        fileButton.addActionListener {
            filePathLabel.text = "loading ..."
            val fileChooser = JFileChooser()
            val result = fileChooser.showOpenDialog(null)

            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fileChooser.selectedFile

                // 拡張子の確認
                if (selectedFile.extension != "xlsx") {
                    alert("File extension must be '.xlsx'")
                    return@addActionListener
                }

                // ファイルの存在確認
                if (!selectedFile.exists()) {
                    try {
                        if (!selectedFile.createNewFile()) {
                            api.logging().logToOutput("Failed to create file:\t${selectedFile.absolutePath}")
                            return@addActionListener
                        }
                    } catch (e: Exception) {
                        api.logging().logToOutput("Error creating file: ${e.message}")
                        return@addActionListener
                    }
                }

                // ファイルが存在する場合の処理
                api.logging().logToOutput("File ${if (selectedFile.exists()) "already exists" else "created"}:\t${selectedFile.absolutePath}")
                outputFile = selectedFile.absolutePath
                filePathLabel.text = selectedFile.name

                if (!selectedFile.exists()) {
                    ExcelTask(api).insertRequestsSheetColumn()
                    requestID = 0
                } else {
                    ExcelTask(api).updateRequestID()
                }

                setMode(2, false)
                api.logging().logToOutput("Set mode:\t$mode")
            }
        }

        // 配置
        c.gridx = 1
        c.gridy = 0
        this.add(JLabel("Excel File"), c)

        c.gridy = 1
        this.add(filePathLabel, c)

        c.gridy = 2
        this.add(fileButton, c)
    }
    private fun addIgnoreBlock() {
        val c = GridBagConstraints()
        c.insets = Insets(5, 10, 5, 10)
        //ブロックタイトル
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 3
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
        c.gridx = 3
        c.gridy = 1
        c.gridheight = 5
        c.gridwidth = 2
        this.add(scrollPane, c)

        // Add Pasteボタン
        val addPasteButton = JButton("Add Paste")
        addPasteButton.toolTipText =
            "Add headers that are excluded from parsing from the clipboard."
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 3
        c.gridy = 14
        c.gridwidth = 1
        this.add(addPasteButton, c)

        // Overwrite Pasteボタン
        val overwritePasteButton = JButton("Overwrite Paste")
        overwritePasteButton.toolTipText =
            "Overwrite headers that are excluded from parsing from the clipboard."
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 4
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
                    api.logging().logToOutput("ignoreHeaderNames:\t${ignoreHeaderNames}")
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
                    api.logging().logToOutput("ignoreHeaderNames:\t${ignoreHeaderNames}")
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
                api.logging().logToOutput("parseScope:\t${parseScope}")
            } else {
                parseScope.remove("Outline")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            }
        }

        pathCheckBox.addActionListener {
            if (pathCheckBox.isSelected) {
                parseScope.add("Path")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            } else {
                parseScope.remove("Path")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            }
        }

        headerCheckBox.addActionListener {
            if (headerCheckBox.isSelected) {
                parseScope.add("Headers")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            } else {
                parseScope.remove("Headers")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            }
        }

        cookiesCheckBox.addActionListener {
            if (cookiesCheckBox.isSelected) {
                parseScope.add("Cookies")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            } else {
                parseScope.remove("Cookies")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            }
        }

        paramsCheckBox.addActionListener {
            if (paramsCheckBox.isSelected) {
                parseScope.add("Params")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            } else {
                parseScope.remove("Params")
                api.logging().logToOutput("parseScope:\t${parseScope}")
            }
        }


        // 配置
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 5
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
                api.logging().logToOutput("valueDecode:\t${valueDecode}")
            } else {
                valueDecode.remove("URL")
                api.logging().logToOutput("valueDecode:\t${valueDecode}")
            }
        }

        // 配置
        c.gridx = 6
        c.gridy = 0
        this.add(JLabel("Value Decoded"), c)
        c.gridy = 1
        this.add(urlDecCheckBox, c)

    }

    private fun alert(message:String){
        JOptionPane.showMessageDialog(this, message, "burpee", JOptionPane.INFORMATION_MESSAGE);
    }

    private fun addSeparator(gridx: Int, gridy: Int) {
        val separator = JSeparator(SwingConstants.VERTICAL)
        val c = GridBagConstraints()
        c.gridx = gridx
        c.gridy = gridy
        c.fill = GridBagConstraints.VERTICAL
        c.insets = Insets(20, 20, 20, 20)
        c.gridheight = GridBagConstraints.REMAINDER
        this.add(separator, c)
    }


}