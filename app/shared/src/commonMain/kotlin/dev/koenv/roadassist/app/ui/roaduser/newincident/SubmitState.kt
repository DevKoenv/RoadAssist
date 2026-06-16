package dev.koenv.roadassist.app.ui.roaduser.newincident

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    data class Error(val message: String) : SubmitState()
    object Success : SubmitState()
}
