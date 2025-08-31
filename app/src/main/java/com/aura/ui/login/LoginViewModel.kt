package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * ViewModel associé à l'écran de connexion.
 * Il gère la logique de connexion et expose l'état de l'UI via un StateFlow.
 */

class LoginViewModel : ViewModel() {

    /** Champ identifier saisi par l'utilisateur */
    private val identifier : MutableStateFlow<String> = MutableStateFlow("")

    fun setIdentifier(newIdentifier: String){
        identifier.value = newIdentifier
    }
    fun setPassword(newPassword: String){
        password.value = newPassword
    }
    /** Champ password saisi par l'utilisateur */
    private val password : MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Flow qui indique si le bouton login doit être activé.
     * Le bouton est activé uniquement si identifier et password ne sont pas vides.
     */
    val isLoginEnabled: StateFlow<Boolean> = combine(identifier, password) { id, pwd ->
        id.isNotBlank() && pwd.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

}