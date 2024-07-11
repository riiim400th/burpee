package burpee

import burp.api.montoya.MontoyaApi
import org.apache.poi.ss.formula.functions.Column
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths

class ExcelTask(private val api: MontoyaApi) {

    private val workbook: XSSFWorkbook = if (File(outputFile).exists() && File(outputFile).length() > 0) {
        FileInputStream(outputFile).use { fileIn ->
            XSSFWorkbook(fileIn)
        }
    } else {
        XSSFWorkbook()
    }

    private var sheet = workbook.getSheet("requests") ?: workbook.createSheet("requests")
    private var currentRowNum = sheet.physicalNumberOfRows
    fun insertRequestsSheetColumn() {
        val column = listOf(listOf("No.","action","Referer URL","Dst URL","Dst URL(with parameters)","Method","Status Code","Parameter count"))
        this.insert(column)
        this.saveWorkbook()
        api.logging().logToOutput("Data inserted to sheet($sheet):\t$column")
    }

    fun selectSheet(name: String) {
        sheet = workbook.getSheet(name) ?: workbook.createSheet(name)
        currentRowNum = sheet.physicalNumberOfRows
        api.logging().logToOutput("Sheet pointed at:\t$name")
    }

    fun updateRequestID() {
        this.selectSheet("requests")
        requestID = ++currentRowNum
        api.logging().logToOutput("updateRequestID:\t$requestID")
    }

    fun insert(data: List<List<Any>>) {
        for (rowData in data) {
            val row = sheet.createRow(currentRowNum++)
            for ((cellIndex, cellData) in rowData.withIndex()) {
                val cell = row.createCell(cellIndex)
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