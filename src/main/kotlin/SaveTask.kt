package burpee

import burp.api.montoya.extension.ExtensionUnloadingHandler


class SaveTask : ExtensionUnloadingHandler {
    private val api = Api.api
    private val settingData = api.persistence().extensionData()

    override fun extensionUnloaded() {
        settingData.setString("data", SaveJsonTask().getJsonString(stateHolder.state))
    }
}