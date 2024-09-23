package burpee

import burp.api.montoya.MontoyaApi

object Api {
    private var _api: MontoyaApi? = null

    val api: MontoyaApi
        get() = _api ?: throw IllegalStateException("API is not initialized")

    fun initializeApi(montoyaApi: MontoyaApi) {
        if (_api == null) {
            _api = montoyaApi
        } else {
            throw IllegalStateException("API has already been initialized")
        }
    }

    fun log(text: String) {
        api.logging().logToOutput(text)
    }
}
