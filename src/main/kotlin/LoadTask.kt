package burpee

import burp.api.montoya.persistence.PersistedObject

class LoadTask(private val savedData: PersistedObject) {
    fun load() {
        val jsonData = savedData.getString("data")
        when {
            (jsonData != null) -> stateHolder.state = LoadJsonTask().getState(jsonData)
        }
    }
}