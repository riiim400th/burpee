package burpee

import burp.api.montoya.utilities.json.JsonObjectNode
import burp.api.montoya.utilities.json.JsonArrayNode
import java.io.File
import java.io.FileWriter
import java.io.IOException

class SaveJsonTask {
    private val api = Api.api
    private fun jsonObject(state: State): JsonObjectNode {
        val jsonObject = JsonObjectNode.jsonObjectNode()
        jsonObject.put(
            "ignoreHeaderNames",
            JsonArrayNode.jsonArrayNode().apply { state.ignoreHeaderNames.forEach { addString(it) } })
        val parseScopeNode = state.parseScope.entries.fold(JsonObjectNode.jsonObjectNode()) { node, (key, value) ->
            node.putBoolean(key, value)
            node
        }
        jsonObject.put("parseScope", parseScopeNode)
        jsonObject.put(
            "valueDecode",
            JsonArrayNode.jsonArrayNode().apply { state.valueDecode.forEach { addString(it) } })
        jsonObject.putBoolean("highlightRows", state.highlightRows)
        jsonObject.putString("outputFile", state.outputFile)
        jsonObject.putNumber("mode", state.mode)
        return jsonObject
    }

    fun getJsonString(state: State): String {
        return jsonObject(state).toJsonString()
    }

    fun exportJson(file: File) {
        // Log before writing to file
        Api.log("Preparing to write to file: ${file.name}")
        val jsonContent = getJsonString(stateHolder.state)
        Api.log("JSON content: $jsonContent")

        try {
            FileWriter(file).use { writer ->
                Api.log("Attempting to write JSON to file")
                writer.write(jsonContent)
                Api.log("Successfully wrote to ${file.name}")
            }
        } catch (e: IOException) {
            api.logging().raiseErrorEvent("Error writing to file: ${e.message}")
            throw e
        }
    }


}