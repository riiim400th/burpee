package burpee

import burp.api.montoya.MontoyaApi
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel

val mode_options = mapOf(
    0 to "clip board",
    1 to "Excel file",
    2 to "clip board & Excel file"
)
var mode = 0
fun checkModeIsValid(inputMode:Int): Boolean{
    return !(inputMode != 0 && outputFile == "")
}
fun setMode(inputMode:Int, force:Boolean) {
    if (force) {
        mode = if (checkModeIsValid(inputMode)) {
            inputMode
        } else {
            0
        }
        comboBox.selectedIndex = mode
    } else {
        if (mode == 0) {
            setMode(inputMode,true)
        }
    }
}
