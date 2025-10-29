package com.aura.ui.transfer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aura.databinding.ActivityTransferBinding
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The transfer activity for the app.
 */
class TransferActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_USER_ID = "com.aura.ui.transfer.SENDER_ID"
  }

  private lateinit var binding: ActivityTransferBinding
  private val transferViewModel: TransferViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupBinding()
    setupInitialData() // Récupération de l'ID de l'expéditeur
    setupInputListeners() // Configuration des écouteurs de texte
    setupTransferButtonListener() // Configuration du bouton de virement
    observeViewModelFlows() // Observation des StateFlows
  }

  /**
   * Configure le View Binding et définit le content view.
   */
  private fun setupBinding() {
    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }

  /**
   * Récupère l'ID de l'expéditeur de l'Intent et le transmet au ViewModel.
   */
  private fun setupInitialData() {
    val userId = intent.getStringExtra(EXTRA_USER_ID)
    if (userId != null) {
      transferViewModel.setSenderId(userId)
    }
    // Si userId est null ici, une gestion d'erreur plus robuste pourrait être nécessaire.
  }

  /**
   * Configure les écouteurs pour les champs de texte (destinataire et montant).
   */
  private fun setupInputListeners() {
    // Utilisation directe de binding.recipient et binding.amount
    binding.recipient.doAfterTextChanged { editable ->
      transferViewModel.setRecipient(editable.toString())
    }
    binding.amount.doAfterTextChanged { editable ->
      transferViewModel.setAmount(editable.toString())
    }
  }

  /**
   * Configure le listener pour le bouton de virement.
   */
  private fun setupTransferButtonListener() {
    // Utilisation directe de binding.transfer
    binding.transfer.setOnClickListener {
      // Début du virement (le chargement sera géré par l'observateur de uiState)
      transferViewModel.transfer()
    }
  }

  /**
   * Démarre l'observation de tous les StateFlows du ViewModel.
   */
  private fun observeViewModelFlows() {
    // Observer le flow pour l'activation du bouton
    lifecycleScope.launch {
      transferViewModel.isTransferButtonEnabled.collectLatest { isEnabled ->
        binding.transfer.isEnabled = isEnabled
      }
    }

    // Observer le flow de l'état global de l'UI
    lifecycleScope.launch {
      transferViewModel.uiState.collectLatest { uiState ->
        handleLoadingState(uiState.isTransferring)
        handleTransferResult(uiState)
      }
    }
  }

  /**
   * Gère la visibilité de la barre de chargement.
   */
  private fun handleLoadingState(isTransferring: Boolean) {
    binding.loading.visibility = if (isTransferring) View.VISIBLE else View.GONE
    // Optionnel : Désactiver l'interaction avec le bouton pendant le chargement
    // binding.transfer.isEnabled = !isTransferring
  }

  /**
   * Gère les résultats du virement (succès ou échec).
   */
  private fun handleTransferResult(uiState: TransferUiState) {
    if (uiState.transferSuccess == true) {
      // Transfert réussie → retour à Home (RESULT_OK)
      setResult(Activity.RESULT_OK, Intent())
      finish()
    }
    // Gérer l'échec (avec message d'erreur)
    else if (uiState.transferSuccess == false && uiState.error != null) {
      // Afficher le message d'erreur provenant du ViewModel
      Toast.makeText(this, getString(uiState.error), Toast.LENGTH_LONG).show()
    }
  }
}