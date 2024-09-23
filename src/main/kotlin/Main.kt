package burpee

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi

const val ver = "v1.2.5"

val stateHolder = StateHolder()

class Main : BurpExtension {
    override fun initialize(api: MontoyaApi) {
        Api.initializeApi(api)
        api.extension().setName("burpee")
        Api.log("========================================\r\nloaded burpee $ver\r\nThis extension is developed by github.com/riiim400th. \r\nFor more information, visit: https://github.com/riiim400th/burpee\r\n========================================\r\n")
        api.userInterface().registerContextMenuItemsProvider(MenuTask())
        api.userInterface().registerSuiteTab("burpee", TabTask())
        api.extension().registerUnloadingHandler(SaveTask())
        LoadTask(api.persistence().extensionData()).load()
    }
}
