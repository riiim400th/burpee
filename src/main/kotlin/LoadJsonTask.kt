package burpee

import java.io.File
import java.io.FileReader
import java.io.IOException
import burp.api.montoya.utilities.json.JsonNode

class LoadJsonTask() {
    fun getState(jsonString: String): State {
        val jsonObject = JsonNode.jsonNode(jsonString).asObject()
        val ignoreHeaderNames =
            jsonObject.get("ignoreHeaderNames").asArray().value.map { it.asString() }.toMutableList()
        val mapNode = jsonObject.get("parseScope").asObject().asMap()
        val parseScope = mapNode.mapValues { (_, v) ->
            v.asBoolean()
        }
        val valueDecode =
            jsonObject.get("valueDecode").asArray().value.map { it.asString() }.toMutableList()
        val highlightRows = jsonObject.get("highlightRows").asBoolean()
        val outputFile = jsonObject.get("outputFile").asString()
        val mode = jsonObject.get("mode").asNumber().toInt()

        return stateHolder.state.copy(
            mode = mode,
            outputFile = outputFile,
            ignoreHeaderNames = ignoreHeaderNames,
            parseScope = parseScope,
            valueDecode = valueDecode,
            highlightRows = highlightRows
        )
    }

    fun loadJson(file: File) {

        Api.log("Preparing to load to file: ${file.name}")

        try {
            FileReader(file).use { reader ->
                Api.log("Attempting to write JSON to file")
                val jsonString = reader.readText()
                if (Api.api.utilities().jsonUtils().isValidJson(jsonString)) {
                    val newState = getState(jsonString)
                    stateHolder.state = newState
                    Api.log("Successfully load from ${file.name}")
                } else {
                    Api.api.logging().raiseErrorEvent("Error: Json format")
                }
            }
        } catch (e: IOException) {
            Api.api.logging().raiseErrorEvent("Error loading file: ${e.message}")
            throw e
        }

    }

}