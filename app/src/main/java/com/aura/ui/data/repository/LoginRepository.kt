package com.aura.ui.data.repository


import com.aura.ui.data.model.LoginRequest
import com.aura.ui.data.model.LoginResponse
import com.aura.ui.data.network.RetrofitInstance

/**
 * Repository qui encapsule l'accès à l'API pour le login
 */
class LoginRepository {

    /**
     * Appelle l'API pour vérifier les identifiants.
     *
     * @param id Identifiant utilisateur
     * @param password Mot de passe
     * @return LoginResponse provenant du serveur
     */
    suspend fun login(id: String, password: String): LoginResponse {
        val request = LoginRequest(id, password)
        return RetrofitInstance.loginApi.login(request)
    }
}