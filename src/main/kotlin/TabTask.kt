package burpee

import burpee.SelectFile.processSelectedFile
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*
import javax.swing.*


class TabTask : JPanel() {
    private val comboBox = JComboBox(
        mapOf(
            0 to "clip board",
            1 to "Excel file",
            2 to "clip board & Excel file"
        ).values.toTypedArray()
    ).apply {
        addActionListener {
            if (comBoxUserChange)
                stateHolder.state = stateHolder.state.copy(mode = this.selectedIndex)

        }
    }
    private var comBoxUserChange = true
    private val filePathLabel = JLabel("No file selected")
    private val listModel = DefaultListModel<String>().apply { addAll(stateHolder.state.ignoreHeaderNames) }
    private val backButton = JButton("←").apply {
        toolTipText = "back"
        isEnabled = false
        addActionListener {
            val ignoreHeaderNames = prevListModel
            prevListModel = listOf()
            stateHolder.state = stateHolder.state.copy(ignoreHeaderNames = ignoreHeaderNames)
        }
    }
    private val outlineCheckBox = getCheckBox("Outline")
    private val pathCheckBox = getCheckBox("Path")
    private val headerCheckBox = getCheckBox("Headers")
    private val paramsCheckBox = getCheckBox("Params")
    private val cookiesCheckBox = getCheckBox("Cookies")
    private val urlDecCheckBox = JCheckBox("URL Decode", stateHolder.state.valueDecode.contains("URL"))
        .apply {
            addActionListener {
                stateHolder.state =
                    stateHolder.state.copy(valueDecode = stateHolder.state.valueDecode.toMutableList().apply {
                        if (isSelected) add("URL") else remove("URL")
                    })
            }
        }
    private val highlightCheckBox = JCheckBox("Reflect color", stateHolder.state.highlightRows).apply {
        toolTipText = "Reflect the highlight color of the Proxy tab onto the rows of the Request sheet."
        addActionListener {
            stateHolder.state = stateHolder.state.copy(highlightRows = this.isSelected)
        }
    }

    private var prevListModel = listOf<String>()

    private fun updateTab(state: State) {
        Api.log("State change")
        comBoxUserChange = false
        comboBox.selectedIndex = state.mode
        comBoxUserChange = true
        filePathLabel.text = fileLabel(state)
        filePathLabel.toolTipText =
            stateHolder.state.outputFile
        listModel.clear()
        listModel.addAll(state.ignoreHeaderNames)
        viewBackButton()
        outlineCheckBox.isSelected = state.parseScope["Outline"]!!
        pathCheckBox.isSelected = state.parseScope["Path"]!!
        headerCheckBox.isSelected = state.parseScope["Headers"]!!
        paramsCheckBox.isSelected = state.parseScope["Params"]!!
        cookiesCheckBox.isSelected = state.parseScope["Cookies"]!!
        urlDecCheckBox.isSelected = state.valueDecode.contains("URL")
        highlightCheckBox.isSelected = state.highlightRows
        Api.log("State is $state")
    }


    init {
        this.layout = GridBagLayout()
        stateHolder.addErrorObserver { errors ->
            alert(errors.joinToString("\r\n"))
            updateTab(stateHolder.state)
        }
        stateHolder.addObserver { state -> updateTab(state) }
        addComponents()
    }

    private fun addComponents() {
        addOutputSection()
        addFileSection()

        addIgnoreHeaderSection()

        addScopeSection()
        addDecodeSection()
        addHighlightSection()
        addUpdateSection()
        addSettingButton()
        addSeparators()
    }

    private fun addComponentToGrid(
        component: JComponent,
        x: Int,
        y: Int,
        gridwidth: Int = 1,
        gridheight: Int = 1,
        anchor: Int = GridBagConstraints.CENTER,
        insets: Insets = Insets(5, 20, 5, 20),
        fill: Int = GridBagConstraints.NONE
    ) {
        val c = GridBagConstraints().apply {
            gridx = x
            gridy = y
            this.gridwidth = gridwidth
            this.gridheight = gridheight
            this.anchor = anchor
            this.insets = insets
            this.fill = fill
        }
        add(component, c)
    }

    private fun addSeparators() {
        addComponentToGrid(JSeparator(SwingConstants.VERTICAL), 2, 0, gridheight = 13, fill = 1)
        addComponentToGrid(
            JSeparator(SwingConstants.HORIZONTAL),
            7,
            2,
            gridwidth = 1,
            fill = 1,
            insets = Insets(15, 20, 20, 15)
        )
        addComponentToGrid(
            JSeparator(SwingConstants.HORIZONTAL),
            7,
            5,
            gridwidth = 1,
            fill = 1,
            insets = Insets(15, 20, 20, 15)
        )
    }

    private fun addOutputSection() {
        addComponentToGrid(JLabel("Output to:"), 0, 0)
        addComponentToGrid(comboBox, 0, 1)
    }

    private fun addFileSection() {
        addComponentToGrid(JLabel("Excel File"), 1, 0, anchor = GridBagConstraints.CENTER)
        addComponentToGrid(filePathLabel, 1, 1, anchor = GridBagConstraints.CENTER)
        addComponentToGrid(JButton("Select File").apply {
            addActionListener {
                filePathLabel.text = "loading ..."
                val fileChange = { file: File ->
                    processSelectedFile(file).takeIf { it.isNotEmpty() }?.let { alert(it) }
                }
                JFileChooser().let { fileChooser ->
                    when (fileChooser.showOpenDialog(null)) {
                        JFileChooser.APPROVE_OPTION -> fileChange(fileChooser.selectedFile)
                        else -> {}
                    }
                }
                filePathLabel.text = fileLabel(stateHolder.state)
            }
        }, 1, 2)
    }

    private fun addIgnoreHeaderSection() {
        addComponentToGrid(JLabel("Ignore Header"), 3, 0, gridwidth = 3)
        addComponentToGrid(JScrollPane(JList(listModel).apply {
            visibleRowCount = 10
            fixedCellWidth = 240
        }), 3, 1, gridwidth = 3, gridheight = 10)

        addComponentToGrid(JButton("Add Paste").apply {
            toolTipText = "Add headers that are excluded from parsing from the clipboard."
            addActionListener {
                updateIgnoreHeader(true)
            }
        }, 3, 11, insets = Insets(5, 5, 5, 5))
        addComponentToGrid(JButton("Overwrite Paste").apply {
            toolTipText = "Overwrite headers that are excluded from parsing from the clipboard."
            addActionListener {
                updateIgnoreHeader(false)
            }
        }, 4, 11, insets = Insets(5, 5, 5, 5))
        addComponentToGrid(backButton, 5, 11, insets = Insets(5, 5, 5, 5))
    }

    private fun addScopeSection() {
        addComponentToGrid(JLabel("Parse Scope"), 6, 0)
        addComponentToGrid(outlineCheckBox, 6, 1, 1, 1, GridBagConstraints.NORTHWEST)
        addComponentToGrid(pathCheckBox, 6, 2, 1, 1, GridBagConstraints.NORTHWEST)
        addComponentToGrid(headerCheckBox, 6, 3, 1, 1, GridBagConstraints.NORTHWEST)
        addComponentToGrid(cookiesCheckBox, 6, 4, 1, 1, GridBagConstraints.NORTHWEST)
        addComponentToGrid(paramsCheckBox, 6, 5, 1, 1, GridBagConstraints.NORTHWEST)
    }

    private fun addDecodeSection() {
        addComponentToGrid(JLabel("Value Decoded"), 7, 0)
        addComponentToGrid(urlDecCheckBox, 7, 1)
    }

    private fun addHighlightSection() {
        addComponentToGrid(JLabel("Request Highlight"), 7, 3)
        addComponentToGrid(highlightCheckBox, 7, 4)
    }

    private fun addSettingButton() {
        addComponentToGrid(JButton(ImageIcon(javaClass.getResource("/setting_icon.png"))).apply {
            isContentAreaFilled = false
            isBorderPainted = false
            isFocusPainted = false
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    settingPopupMenu().show(e.component, e.x, e.y)
                }
            })
        }, 8, 0, anchor = GridBagConstraints.WEST, insets = Insets(0, 0, 0, 0))
    }

    private fun addUpdateSection() {
        val updateInfo = PollUpdate(ver).poll()
        if (updateInfo.updatable) {
            addComponentToGrid(JLabel("Latest Version"), 7, 7)
            addComponentToGrid(JButton("Update to ${updateInfo.latest.tagName}").apply {
                toolTipText = "Latest version ${updateInfo.latest.tagName} has been released. \nPlease download it from here."
                addActionListener {
                    try {
                        Desktop.getDesktop().browse(URI(updateInfo.latest.htmlUrl))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, 7, 8)
        }
    }


    private fun updateIgnoreHeader(add: Boolean) {
        prevListModel = List(listModel.size()) { i -> listModel.get(i) }
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val contents: Transferable? = clipboard.getContents(null)
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                val pasteText = contents.getTransferData(DataFlavor.stringFlavor) as String
                val items = pasteText.split("\n").filter { it.isNotEmpty() }

                val ignoreHeaderNames = when (add) {
                    true -> stateHolder.state.ignoreHeaderNames.toMutableList().apply { addAll(items) }
                        .toCollection(LinkedHashSet()).toMutableList()

                    false -> items
                }
                stateHolder.state = stateHolder.state.copy(ignoreHeaderNames = ignoreHeaderNames)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun truncate(text: String): String {
        return when (text.length > 15) {
            true -> "${text.take(5)}...${text.takeLast(7)}"
            false -> text
        }
    }

    private fun fileLabel(state: State): String {
        val text = File(state.outputFile).name
        return if (text == "") {
            "No file selected"
        } else {
            truncate(text)
        }
    }

    private fun viewBackButton() {
        backButton.isEnabled = prevListModel.isNotEmpty()
    }

    private fun getCheckBox(scopeName: String): JCheckBox {
        val parseScope = stateHolder.state.parseScope
        return JCheckBox(scopeName, parseScope[scopeName] ?: false)
            .apply {
                addActionListener {
                    val updateParseScope = stateHolder.state.parseScope.toMutableMap()
                    updateParseScope[scopeName] = isSelected
                    stateHolder.state = stateHolder.state.copy(parseScope = updateParseScope)
                }
            }
    }

    private fun showFileChooserDialog(action: String, onFileSelected: (File) -> Unit) {
        val fileChooser = JFileChooser()
        val result = if (action == "save") fileChooser.showSaveDialog(null) else fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile

            // Check file extension
            if (selectedFile.extension.lowercase() != "json") {
                JOptionPane.showMessageDialog(
                    null,
                    "File extension must be '.json'",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }
            onFileSelected(selectedFile)
        }
    }

    private fun settingPopupMenu(): JPopupMenu {
        return JPopupMenu().apply {

            val saveItem = JMenuItem("Save Setting").apply {
                addActionListener {
                    showFileChooserDialog("save") { selectedFile ->
                        try {
                            SaveJsonTask().exportJson(selectedFile)
                        } catch (e: IOException) {
                            alert("Failed to write to ${selectedFile.name}: ${e.message}")
                        }
                    }
                }
            }
            val loadMenuItem = JMenuItem("Load Setting")
                .apply {
                    addActionListener {
                        showFileChooserDialog("open") { selectedFile ->
                            try {
                                LoadJsonTask().loadJson(selectedFile)
                            } catch (e: IOException) {
                                alert("Failed to load ${selectedFile.name}: ${e.message}")
                            }
                        }
                    }
                }

            val resetMenuItem = JMenuItem("Reset Setting")
                .apply {
                    addActionListener {
                        showConfirmationDialog(
                            "Reset setting?",
                            "Confirmation"
                        ) { isOk ->
                            if (isOk) {
                                stateHolder.state = DefaultData.defaultState
                            }
                        }
                    }
                }

            add(saveItem)
            add(loadMenuItem)
            add(resetMenuItem)
        }
    }

    private fun alert(message: String) {
        JOptionPane.showMessageDialog(this, message, "burpee", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun showConfirmationDialog(message: String, title: String, callback: (Boolean) -> Unit) {
        val result = JOptionPane.showConfirmDialog(
            null,
            message,
            title,
            JOptionPane.OK_CANCEL_OPTION
        )
        callback(result == JOptionPane.OK_OPTION)
    }

}