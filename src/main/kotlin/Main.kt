package burpee

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi

const val ver = "v1.2.4"
class Main : BurpExtension{
    override fun initialize(api: MontoyaApi) {
        val logging = api.logging()
        api.extension().setName("burpee")
        logging.logToOutput("loaded burpee $ver\r\n")
        val menuTask = MenuTask(api)
        api.userInterface().registerContextMenuItemsProvider(menuTask)
        api.userInterface().registerSuiteTab("burpee", TabTask(api))
    }
}