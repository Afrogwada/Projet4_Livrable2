package com.aura.ui.login

import com.aura.ui.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ui.data.LoginResponse
import com.aura.ui.repository.LoginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel associé à l'écran de connexion.
 * Il gère la logique de connexion et expose l'état de l'UI via un StateFlow.
 * Inclut la gestion des erreurs réseau.
 */

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    /** Champ identifier saisi par l'utilisateur */
    private val identifier : MutableStateFlow<String> = MutableStateFlow("")

    fun setIdentifier(newIdentifier: String){
        identifier.value = newIdentifier
    }
    /** Champ password saisi par l'utilisateur */
    private val password : MutableStateFlow<String> = MutableStateFlow("")

    fun setPassword(newPassword: String){
        password.value = newPassword
    }

    fun setLoginGranted(granted: Boolean){
        _loginResult.value = _loginResult.value?.copy(granted = granted)
    }

    /** Résultat de la tentative de connexion */
    private val _loginResult = MutableStateFlow<LoginResponse?>(null)
    val loginResult: StateFlow<LoginResponse?> = _loginResult.asStateFlow()

    /** Indique si une erreur réseau est survenue */
    private val _networkError = MutableStateFlow(false)
    val networkError: StateFlow<Boolean> = _networkError

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

    /**
     * Lance la connexion via le repository.
     * Gère les exceptions réseau.
     */
    fun login() {

        viewModelScope.launch {

            try {
                Logger.d("set login user id ${identifier.value}")
                Logger.d("set login user pass ${password.value}")
                val response = repository.login(identifier.value, password.value)
                _loginResult.value = LoginResponse(granted = response.granted)
                Logger.d(response.toString())
                Logger.d("response.granted = ${response.granted}")
                Logger.d("_loginResult.value to string = ${_loginResult.value.toString()}")
                Logger.d("_loginResult.value = ${_loginResult.value}")
            } catch (e: IOException) {
                // Erreur réseau (pas de connexion, timeout, etc.)
                _networkError.value = true
                setLoginGranted(false)
                Logger.d("Erreur réseau (pas de connexion, timeout, etc.) ${e.message}")
            } catch (e: Exception) {
                setLoginGranted(false)
                Logger.d("Erreur réseau Login ${e.message}")
                Logger.d("Autre erreur")
            }
        }
    }

}