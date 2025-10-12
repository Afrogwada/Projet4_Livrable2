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
import com.aura.ui.Logger
import com.aura.ui.home.HomeActivity
import com.aura.ui.home.HomeActivity.Companion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The transfer activity for the app.
 */
class TransferActivity : AppCompatActivity() {

  companion object {
    const val EXTRA_USER_ID = "com.aura.ui.transfer.SENDER_ID"
  }

  /**
   * The binding for the transfer layout.
   */
  private lateinit var binding: ActivityTransferBinding

  private val transferViewModel: TransferViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTransferBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val recipient = binding.recipient
    val amount = binding.amount
    val transfer = binding.transfer
    val loading = binding.loading

    // Récupération de l'ID utilisateur de l'Intent (transmis par HomeActivity)
    val userId = intent.getStringExtra(EXTRA_USER_ID)
    if (userId != null) {
      transferViewModel.setSenderId(userId)
    }


    /**
     * Configure les listeners
     */

    transfer.setOnClickListener {
      loading.visibility = View.VISIBLE
      transferViewModel.performTransfer()

    }
    recipient.doAfterTextChanged { editable ->
      transferViewModel.setRecipient(editable.toString())
    }

    amount.doAfterTextChanged { editable ->
      transferViewModel.setAmount(editable.toString())
    }

    /**
     * Observe le StateFlow pour activer/désactiver le bouton de virement.
     */

    lifecycleScope.launch {
      transferViewModel.isTransferButtonEnabled.collectLatest { isEnabled ->
        // Désactiver/Activer le bouton de virement
        transfer.isEnabled = isEnabled
      }
    }
    // ---------------------------------------------------------------------------------------------
    // NOUVEL OBSERVATEUR UNIQUE DU CONTENEUR D'ÉTAT (uiState)
    // ---------------------------------------------------------------------------------------------
    lifecycleScope.launch {
      transferViewModel.uiState.collectLatest { uiState ->
        // Gérer l'état de chargement
        loading.visibility = if (uiState.isTransferring) View.VISIBLE else View.GONE
        //transfer.isEnabled = !uiState.isTransferring // Désactiver le bouton pendant le chargement

        if (uiState.transferSuccess == true) {
          // Transfert réussie → retour à Home
          setResult(Activity.RESULT_OK, Intent())
          finish()
        }
        // 3. Gérer l'échec (avec ou sans message d'erreur)
        else if (uiState.transferSuccess == false && uiState.error != null) {
          // Afficher le message d'erreur provenant du ViewModel
          Toast.makeText(this@TransferActivity, getString(uiState.error), Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}



