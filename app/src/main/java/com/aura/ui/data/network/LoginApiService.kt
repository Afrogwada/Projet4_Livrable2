package com.aura.ui.data.network

import com.aura.ui.data.model.LoginRequest
import com.aura.ui.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface Retrofit pour accéder aux endpoints de l'API.
 */
interface LoginApiService {

    /**
     * Endpoint POST pour la connexion de l'utilisateur.
     *
     * @param request Objet contenant l'identifiant et le mot de passe
     * @return LoginResponse indiquant si l'accès est accordé
     */
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}