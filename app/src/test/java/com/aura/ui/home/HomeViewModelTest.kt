package com.aura.ui.home

import org.junit.Test
import org.junit.Before
import io.mockk.*
import kotlinx.coroutines.flow.first
import java.io.IOException

import com.aura.R
import com.aura.data.model.AccountResponse
import com.aura.ui.MainDispatcherRule

import com.aura.data.repository.GetAccountsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.fail
import org.junit.Rule
import retrofit2.HttpException
import retrofit2.Response


class HomeViewModelTest {
    // Assure que le Dispatcher.Main est disponible pour les tests coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Nécessite la classe MainDispatcherRule

    // Déclarer le Mock Le Mock : Crée un faux objet de type GetAccountsRepository en utilisant MockK. Cet objet ne fera rien par défaut ; nous lui dirons quoi répondre dans chaque test.
    private val mockRepository: GetAccountsRepository = mockk()

    // L'objet à tester
    private lateinit var viewModel: HomeViewModel


    // Préparer le ViewModel avant chaque test
    @Before
    fun setUp() {
        // Initialiser le ViewModel avec le mock
        clearAllMocks() // <-- Réinitialise tous les mocks avant chaque test
        viewModel = HomeViewModel(mockRepository)

    }

    private suspend fun performLoadUserAccounts(
        mockResponse: suspend () -> Unit,
        successExpected: Boolean,
        expectedBalance: Double = 0.0,
        testUserId: String = "test_user",
        errorExpected: Int? = null
    ) {
        // ARRANGE  : Préparer les données et le mock
        mockResponse()


        // ACT : Appeler la fonction à tester
        viewModel.loadUserAccounts(testUserId)
        val finalState = viewModel.uiState.first()

        // ASSERT : Vérifier l'état final

        assertFalse("isLoading doit être false après LoadUserAccounts", finalState.isLoading)
        assertEquals(successExpected, finalState.isSuccess)
        if (successExpected) {
            assertTrue(
                "La balance ne correspond pas à l'attendu",
                expectedBalance == finalState.balance
            )
            assertTrue(
                "L'identifiant utilisateur n'a pas été mis à jour",
                finalState.userId == testUserId
            )
        }
        if (errorExpected != null) {
            assertTrue(
                "le message d'erreur devrait être : " + errorExpected,
                finalState.error == errorExpected
            )
        } else {
            assertTrue(
                "Il ne devrait pas y avoir de message d'erreur",
                finalState.error == errorExpected
            )
        }

        // Vérifier que le repository a bien été appelé une seule fois avec le bon ID
        coVerify(exactly = 1) { mockRepository.getAccounts(any()) }


    }

    @Test
    fun success_state() = runTest {
        val expectedBalance = 1500.00
        val testUserId = "test_user"
        performLoadUserAccounts(
            successExpected = true,
            expectedBalance = expectedBalance,
            testUserId = testUserId,
            errorExpected = null,
            mockResponse = {
                coEvery { mockRepository.getAccounts(any()) } returns listOf(
                    AccountResponse(
                        id = testUserId,
                        balance = expectedBalance,
                        isMain = true
                    ),
                    AccountResponse(
                        id = "testUser",
                        balance = 50.00,
                        isMain = false
                    )
                )
            }
        )
    }

    @Test
    fun network_error() = runTest {
        performLoadUserAccounts(
            successExpected = false,
            errorExpected = R.string.error_network,
            mockResponse = { coEvery { mockRepository.getAccounts(any()) } throws IOException("No internet") }
        )
    }

    @Test
    fun id_error() = runTest {
        val httpException404 = HttpException(
            Response.error<Any>(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )
        )
        performLoadUserAccounts(
            successExpected = false,
            testUserId = "bad_Id",
            errorExpected = R.string.error_user_not_found,
            mockResponse = { coEvery { mockRepository.getAccounts(any()) } throws httpException404 }
        )
    }

    @Test
    fun other_error() = runTest {
        performLoadUserAccounts(
            successExpected = false,
            errorExpected = R.string.error_generic,
            mockResponse = { coEvery { mockRepository.getAccounts(any()) } throws RuntimeException("Unexpected error") }
        )
    }

}