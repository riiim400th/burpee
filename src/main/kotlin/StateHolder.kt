package burpee

class StateHolder {
    private val observers = mutableListOf<(State) -> Unit>()
    private val errorObservers = mutableListOf<(List<String>) -> Unit>()
    private var _state = DefaultData.defaultState

    var state: State
        get() = _state
        set(state) {
            val errors = Validation.validState(state)
            if (errors.isEmpty()) {
                _state = state
                notifyObservers()
            } else {
                notifyErrorObservers(errors)
            }

        }

    fun addObserver(observer: (State) -> Unit) {
        observers.add(observer)
    }

    fun addErrorObserver(observer: (List<String>) -> Unit) {
        errorObservers.add(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it(_state) }
    }

    private fun notifyErrorObservers(errorList: List<String>) {
        errorObservers.forEach { it(errorList) }
    }

}