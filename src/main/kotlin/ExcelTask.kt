package burpee

import burp.api.montoya.MontoyaApi
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.poi.ss.usermodel.*

class ExcelTask(private val api: MontoyaApi) {

    private val workbook: XSSFWorkbook = if (File(outputFile).exists() && File(outputFile).length() > 0) {
        FileInputStream(outputFile).use { fileIn ->
            XSSFWorkbook(fileIn)
        }
    } else {
        XSSFWorkbook()
    }
    private var cellStyle = workbook.createCellStyle()

    private var sheet = workbook.getSheet("requests") ?: workbook.createSheet("requests")
    private var currentRowNum = sheet.physicalNumberOfRows
    fun insertRequestsSheetColumn() {
        val column = listOf(listOf("No.","action","Referer URL","Dst URL","Dst URL(with parameters)","Method","Status Code","Parameter count","Note"))
        highlightRows = true
        this.insert(column,true,"DEFAULT")
        this.saveWorkbook()
        highlightRows = false
        requestID = currentRowNum
        api.logging().logToOutput("Data inserted to sheet($sheet):\t$column")
    }

    fun selectSheet(name: String) {
        sheet = workbook.getSheet(name) ?: workbook.createSheet(name)
        cellStyle = workbook.createCellStyle()
        currentRowNum = sheet.physicalNumberOfRows
        api.logging().logToOutput("Sheet pointed at:\t$name")
    }

    fun updateRequestID() {
        this.selectSheet("requests")
        requestID = currentRowNum
        api.logging().logToOutput("updateRequestID:\t$requestID")
    }

    fun insert(data: List<List<Any>>, bold: Boolean = false, color: String = "") {

        if (bold) {
            val font = workbook.createFont()
            font.bold = true
            cellStyle.setFont(font)
        }

        if (color.isNotEmpty()&&highlightRows) {
            val indexedColors = when (color){
                "RED" -> IndexedColors.RED
                "ORANGE" -> IndexedColors.LIGHT_ORANGE
                "YELLOW" -> IndexedColors.YELLOW
                "GREEN" -> IndexedColors.LIME
                "CYAN" -> IndexedColors.AQUA
                "BLUE" -> IndexedColors.INDIGO
                "PINK" -> IndexedColors.ROSE
                "MAGENTA" -> IndexedColors.PINK
                "GRAY" -> IndexedColors.GREY_40_PERCENT
                "DEFAULT" -> IndexedColors.GREY_50_PERCENT
                else -> IndexedColors.WHITE
            }
            cellStyle.fillForegroundColor = indexedColors.index
            cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        cellStyle.borderTop = BorderStyle.THIN
        cellStyle.borderBottom = BorderStyle.THIN
        cellStyle.borderLeft = BorderStyle.THIN
        cellStyle.borderRight = BorderStyle.THIN
        for (rowData in data) {
            val row = sheet.createRow(currentRowNum++)
            for ((cellIndex, cellData) in rowData.withIndex()) {
                val cell = row.createCell(cellIndex)

                    cell.cellStyle = cellStyle

                setCellValue(cell, cellData)
            }
        }

        api.logging().logToOutput("Data inserted to sheet($sheet):\t$data")
    }

    private fun setCellValue(cell: Cell, value: Any) {
        when (value) {
            is String -> cell.setCellValue(value)
            is Double -> cell.setCellValue(value)
            is Int -> cell.setCellValue(value.toDouble())
            else -> cell.setCellValue(value.toString())
        }
    }

    fun saveWorkbook() {
        FileOutputStream(outputFile).use { fileOut ->
            workbook.write(fileOut)
        }
        api.logging().logToOutput("Workbook saved to:\t$outputFile")
    }
}