package dev.koenv.roadassist.app

import dev.koenv.roadassist.core.Role

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    data class Success(val role: Role) : LoginState()
}
