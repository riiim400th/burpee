package burpee

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi

class Main : BurpExtension{
    override fun initialize(api: MontoyaApi) {
        val logging = api.logging()
        api.extension().setName("burpee")
        logging.logToOutput("loaded burpee v1.0.0\r\n")
        val menuTask = MenuTask(api)
        api.userInterface().registerContextMenuItemsProvider(menuTask)
    }
}