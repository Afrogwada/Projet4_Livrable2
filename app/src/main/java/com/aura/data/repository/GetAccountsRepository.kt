package com.aura.data.repository

import com.aura.data.model.AccountResponse
import com.aura.data.network.RetrofitInstance

/**
 * Repository qui encapsule l'accès à l'API pour les comptes.
 */
class GetAccountsRepository {

    /**
     * Récupère la liste des comptes pour un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Liste des comptes.
     */
    suspend fun getAccounts(userId: String): List<AccountResponse> {
        return RetrofitInstance.getAccounts.getAccounts(userId)
    }
}