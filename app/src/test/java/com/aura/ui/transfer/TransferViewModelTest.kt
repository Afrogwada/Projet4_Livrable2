package com.aura.ui.transfer

import org.junit.Test
import org.junit.Before
import io.mockk.*
import kotlinx.coroutines.flow.first
import java.io.IOException

import com.aura.R
import com.aura.data.model.TransferResponse
import com.aura.data.model.TransferRequest
import com.aura.ui.MainDispatcherRule

import com.aura.data.repository.TransferRepository
import com.aura.ui.transfer.TransferViewModel
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

class TransferViewModelTest {
    // Assure que le Dispatcher.Main est disponible pour les tests coroutines
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Nécessite la classe MainDispatcherRule

    // Déclarer le Mock Le Mock : Crée un faux objet de type TransferRepository en utilisant MockK. Cet objet ne fera rien par défaut ; nous lui dirons quoi répondre dans chaque test.
    private val mockRepository: TransferRepository = mockk()

    // L'objet à tester
    private lateinit var viewModel: TransferViewModel

    private val testRecipient="Test_Recipient"
    private val testAmount= "10.10"
    private val testSender = "Test_Sender"


    // Préparer le ViewModel avant chaque test
    @Before
    fun setUp() {
        // Initialiser le ViewModel avec le mock
        clearAllMocks() // <-- Réinitialise tous les mocks avant chaque test
        viewModel = TransferViewModel(mockRepository)
        viewModel.setRecipient(testRecipient)
        viewModel.setAmount(testAmount)

    }

    // Tester l'Activation du Bouton Login (isLoginEnabled)
    private suspend fun checkTransferButton(recipient: String = testRecipient, amount: String = testAmount, expected: Boolean, message: String) {
        // ARRANGE : Simuler la saisie des données initiales
        viewModel.setRecipient(recipient)
        viewModel.setAmount(amount)

        // ACT
        val finalState = viewModel.isTransferButtonEnabled.first()

        // ASSERT : Vérifier l'état du bouton Login
        if (expected) {
            assertTrue(message,finalState)
        } else {
            assertFalse(message,finalState)
        }
    }

    @Test
    fun isTransferEnabled_true()= runTest{
        checkTransferButton(
            recipient = "test_user",
            amount = "test_amount",
            expected = true,
            message = "Le bouton de transfert devrait être activé car le montant et l'identidiant sont présents"
        )
    }

    @Test
    fun isTransferEnabled_false_amount_missing()= runTest{
        checkTransferButton(
            recipient = "test_user",
            amount = "",
            expected = false,
            message = "Le bouton de transfert ne devrait pas être activé car le montant est manquant"
        )
    }

    @Test
    fun isTransferEnabled_false_recipient_missing()= runTest{
        checkTransferButton(
            recipient = "",
            amount = "test_amount",
            expected = false,
            message = "Le bouton de transfert ne devrait pas être activé car l'identifiant est manquant"
        )
    }

    // Tester la Logique de Transfert (transfer())
    private suspend fun testTransfer(
        mockResponse: suspend () -> Unit,
        successExpected: Boolean,
        errorExpected: Int?=null,
        verifyCall: Boolean = true
    )
    {
        // ARRANGE : Simuler la saisie des données initiales
        mockResponse()
        viewModel.setSenderId(testSender)

        // ACT
        viewModel.transfer()
        val finalState = viewModel.uiState.first()

        // ASSERT : Vérifier l'état final
        assertFalse( "isTransferring doit être false après transfer()",finalState.isTransferring)
        assertEquals("L'état de succès du transfert n'est pas correct", successExpected, finalState.transferSuccess)

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

        if (verifyCall) {
            val expectedRequest = TransferRequest(
                sender = testSender,
                recipient = testRecipient,
                amount = testAmount.toDouble()
            )
            try {
                coVerify(exactly = 1) { mockRepository.performTransfer(expectedRequest) }
            } catch (e: AssertionError) {
                fail("La fonction performTransfer() du repository n'a pas été appelée correctement : ${e.message}")
            }
        } else {
            // S'assurer qu'aucun appel n'a été fait (ex: validation échouée)
            coVerify(exactly = 0) { mockRepository.performTransfer(any()) }
        }
    }

    @Test
    fun transfer_succes()= runTest {
        testTransfer(
            mockResponse = {coEvery { mockRepository.performTransfer(any()) } returns TransferResponse(result = true) },
            successExpected = true,
            errorExpected = null
        )

    }

    @Test
    fun transfert_failed_server_error()= runTest {
        testTransfer(
            mockResponse = {coEvery { mockRepository.performTransfer(any()) } returns TransferResponse(result = false) },
            successExpected = false,
            errorExpected = R.string.transfert_failure // Attendre l'erreur d'échec de transfert
        )
    }

    @Test
    fun transfer_invalid_amount_zero()= runTest {
        viewModel.setAmount("0.0")
        testTransfer(
            mockResponse = { /* Pas besoin de mock, la validation échoue avant */ },
            successExpected = false,
            errorExpected = R.string.amount_invalid, // Attendre l'erreur de montant invalide
            verifyCall = false // S'assurer que performTransfer n'est PAS appelé
        )
    }

    @Test
    fun transfer_invalid_amount_null()= runTest {
        viewModel.setAmount("null")
        testTransfer(
            mockResponse = { /* Pas besoin de mock, la validation échoue avant */ },
            successExpected = false,
            errorExpected = R.string.amount_invalid, // Attendre l'erreur de montant invalide
            verifyCall = false // S'assurer que performTransfer n'est PAS appelé
        )
    }

    @Test
    fun transfer_network_error()= runTest{
        testTransfer(
            mockResponse = { coEvery { mockRepository.performTransfer(any()) } throws IOException()},
            successExpected = false,
            errorExpected = R.string.error_network
        )
    }
    @Test
    fun other_error()= runTest{
        testTransfer(
            mockResponse = { coEvery {  mockRepository.performTransfer(any()) } throws Exception("Unknown Error")},
            successExpected = false,
            errorExpected = R.string.error_generic
        )
    }


}