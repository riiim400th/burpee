package burpee

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileNotFoundException
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.poi.ss.usermodel.*

class ExcelTask(private val state: State) {


    private val workbook: XSSFWorkbook = if (File(state.outputFile).exists() && File(state.outputFile).length() > 0) {
        FileInputStream(state.outputFile).use { fileIn ->
            XSSFWorkbook(fileIn)
        }
    } else {
        XSSFWorkbook()
    }
    private var sheet = workbook.getSheet("requests") ?: workbook.createSheet("requests")
    private var currentRowNum = sheet.physicalNumberOfRows

    fun insertRequestsSheetColumn(): ExcelTask {
        val column = listOf(
            DefaultData.requestSheetColumn
        )
        this.insert(column, true, "GRAY_50")
        return this
    }

    fun selectSheet(name: String = "requests"): ExcelTask {
        sheet = workbook.getSheet(name) ?: workbook.createSheet(name)
        currentRowNum = sheet.physicalNumberOfRows
        return this
    }

    fun getRequestID(): Int {
        return when (state.outputFile) {
            "" -> 0
            else -> this.selectSheet("requests").currentRowNum
        }
    }

    fun insert(data: List<List<Any>>, bold: Boolean = false, color: String = ""): ExcelTask {
        val cellStyle = workbook.createCellStyle()
        if (bold) {
            val font = workbook.createFont()
            font.bold = true
            cellStyle.setFont(font)
        }

        val indexedColors = when (color) {
            "RED" -> IndexedColors.RED
            "ORANGE" -> IndexedColors.LIGHT_ORANGE
            "YELLOW" -> IndexedColors.YELLOW
            "GREEN" -> IndexedColors.LIME
            "CYAN" -> IndexedColors.AQUA
            "BLUE" -> IndexedColors.INDIGO
            "PINK" -> IndexedColors.ROSE
            "MAGENTA" -> IndexedColors.PINK
            "GRAY" -> IndexedColors.GREY_40_PERCENT
            "GRAY_50" -> IndexedColors.GREY_50_PERCENT
            else -> IndexedColors.WHITE
        }
        cellStyle.fillForegroundColor = indexedColors.index
        cellStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        cellStyle.borderTop = BorderStyle.THIN
        cellStyle.borderBottom = BorderStyle.THIN
        cellStyle.borderLeft = BorderStyle.THIN
        cellStyle.borderRight = BorderStyle.THIN

        data.forEach { row ->
            val targetRow = sheet.createRow(currentRowNum++)
            row.forEachIndexed() { i, cell ->
                targetRow.createCell(i).apply { this.cellStyle = cellStyle }.setCellValue(cell.toString())
            }
        }
        return this
    }

    fun saveWorkbook(): ExcelTask {
        try {
            FileOutputStream(state.outputFile).use { fileOut ->
                workbook.write(fileOut)
            }
            return this
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException()
        }

    }
}