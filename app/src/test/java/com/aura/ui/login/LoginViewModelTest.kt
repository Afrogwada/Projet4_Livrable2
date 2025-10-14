package com.aura.ui.login

import org.junit.Test
import org.junit.Before
import io.mockk.*
import kotlinx.coroutines.flow.first
import java.io.IOException

import com.aura.R
import com.aura.ui.MainDispatcherRule
import com.aura.ui.data.model.LoginResponse
import com.aura.ui.data.repository.LoginRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Rule

class LoginViewModelTest {
    // Assure que le Dispatcher.Main est disponible pour les tests coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Nécessite la classe MainDispatcherRule

    // Déclarer le Mock Le Mock : Crée un faux objet de type LoginRepository en utilisant MockK. Cet objet ne fera rien par défaut ; nous lui dirons quoi répondre dans chaque test.
    private val mockRepository: LoginRepository = mockk()

    // L'objet à tester
    private lateinit var viewModel: LoginViewModel


    // Préparer le ViewModel avant chaque test
    @Before
    fun setUp() {
        // Initialiser le ViewModel avec le mock
        clearAllMocks() // <-- Réinitialise tous les mocks avant chaque test
        viewModel = LoginViewModel(mockRepository)

    }

    // Tester la Logique de Connexion (login())
    private suspend fun performLogin(
        mockResponse: suspend () -> Unit,
        successExpected: Boolean,
        userIdExpected: String? = null,
        errorExpected: Int?=null
        )
        {
            // ARRANGE
            mockResponse()
            viewModel.setIdentifier("test_user")
            viewModel.setPassword("test_pwd")

            // ACT
            viewModel.login()
            val finalState = viewModel.uiState.first()

            // ASSERT : Vérifier l'état final
            assertFalse( "Le chargement devrait être terminé après login",finalState.isLoading)
            assertEquals(successExpected, finalState.isSuccess)
            if (userIdExpected != null) {
                assertTrue(
                    "L'identifiant utilisateur n'a pas été mis à jour",
                    finalState.userId == userIdExpected
                )
            } else {
                assertTrue(
                    "L'identifiant utilisateur n'aurait pas dû être mis à jour",
                    finalState.userId == userIdExpected
                )
            }
            if (errorExpected != null) {
                assertTrue(
                    "le message d'erreur devrait être : "+errorExpected,
                    finalState.error == errorExpected
                )
            } else {
                assertTrue(
                    "Il ne devrait pas y avoir de message d'erreur",
                    finalState.error == errorExpected
                )
            }
            try {
                coVerify(exactly = 1) { mockRepository.login("test_user", "test_pwd") }
            } catch (e: AssertionError){
                fail("La fonction login() du repository n'a pas été appelée correctement : ${e.message}")
            }
        }

    @Test
    fun login_succes()= runTest {
        performLogin(
            mockResponse = {
                coEvery { mockRepository.login(any(), any()) } returns LoginResponse(
                    granted = true,
                    userId = "test_user"
                )
            },
            successExpected = true,
            userIdExpected = "test_user",
            errorExpected = null
        )
    }
    @Test
    fun login_failed()= runTest {
        performLogin(
            mockResponse = {
                coEvery { mockRepository.login(any(), any()) } returns LoginResponse(
                    granted = false,
                    userId = "test_user"
                )
            },
            successExpected = false,
            userIdExpected = null,
            errorExpected = R.string.error_bad_id
        )
    }
    @Test
    fun login_network_error()= runTest{
        performLogin(
            mockResponse = { coEvery { mockRepository.login(any(), any()) } throws IOException("No internet") },
            successExpected=false,
            userIdExpected=null,
            errorExpected=R.string.error_network
        )
    }
    @Test
    fun other_error()= runTest{
        performLogin(
            mockResponse = { coEvery { mockRepository.login(any(), any()) } throws RuntimeException("Unexpected error") },
            successExpected=false,
            userIdExpected=null,
            errorExpected=R.string.error_generic
        )
    }


    // Tester l'Activation du Bouton Login (isLoginEnabled)
    private suspend fun checkLoginButton(identifier: String, password: String, expected: Boolean, message: String) {
        // ARRANGE : Simuler la saisie des données initiales
        viewModel.setIdentifier(identifier)
        viewModel.setPassword(password)

        // ACT
        val finalState = viewModel.isLoginEnabled.first()

        // ASSERT : Vérifier l'état du bouton Login
        if (expected) {
            assertTrue(message,finalState)
        } else {
            assertFalse(message,finalState)
        }
    }
    @Test
    fun isLoginEnabled_true()= runTest{
        checkLoginButton(
            identifier = "test_user",
            password = "test_pwd",
            expected = true,
            message = "Le bouton de login devrait être activé car le mot de passe et l'identidiant sont présents"
        )
    }
    @Test
    fun isLoginEnabled_false_password_missing()= runTest{
        checkLoginButton(
            identifier = "test_user",
            password = "",
            expected = false,
            message = "Le bouton de login ne devrait pas être activé car le mot de passe est manquant"
        )
    }
    @Test
    fun isLoginEnabled_false_identifier_missing()= runTest{
        checkLoginButton(
            identifier = "",
            password = "test_pwd",
            expected = false,
            message = "Le bouton de login ne devrait pas être activé car l'identifiant est manquant"
        )
    }
}