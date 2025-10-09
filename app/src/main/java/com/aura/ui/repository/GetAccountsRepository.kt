package com.aura.ui.repository

import com.aura.ui.data.AccountResponse
import com.aura.ui.network.RetrofitInstance

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