package com.aura.ui.transfer

import android.app.Activity
import android.os.Bundle
import android.view.View
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


    /**
     * Configure les listeners
     */

    transfer.setOnClickListener {
      loading.visibility = View.VISIBLE
      transferViewModel.performTransfer()
      // Simuler la fin du virement et fermer l'activité
      setResult(Activity.RESULT_OK)
      finish()
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


  }
}



