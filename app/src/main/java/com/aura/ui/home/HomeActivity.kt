package com.aura.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aura.R
import com.aura.databinding.ActivityHomeBinding
import com.aura.ui.login.LoginActivity
import androidx.lifecycle.lifecycleScope
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * The home activity for the app.
 */
class HomeActivity : AppCompatActivity()
{
  // CONSTANTE pour l'ID utilisateur dans l'Intent
  companion object {
    const val EXTRA_USER_ID = "com.aura.ui.home.USER_ID"
  }
  /**
   * The binding for the home layout.
   */
  private lateinit var binding: ActivityHomeBinding

  /** ViewModel associé à cet écran. */
  private val homeViewModel: HomeViewModel by viewModels()

  /**
   * A callback for the result of starting the TransferActivity.
   */
  private val startTransferActivityForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      //TODO
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Récupération de l'ID utilisateur de l'Intent (transmis par LoginActivity)
    val userId = intent.getStringExtra(EXTRA_USER_ID)

    val balance = binding.balance
    val transfer = binding.transfer
    val loading = binding.loading

    if (!userId.isNullOrBlank()) {
      // Déclenchement de l'appel API dans le ViewModel
      homeViewModel.loadUserAccounts(userId)
    } else {
      // Cas d'erreur : ID manquant (ex: redirection vers LoginActivity)
      startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
      finish()
    }


    transfer.setOnClickListener {
      startTransferActivityForResult.launch(Intent(this@HomeActivity, TransferActivity::class.java))
    }

    /**
     * Gère l'appui sur le bouton de retour.
     * Appelle finish() pour fermer la HomeActivity et quitter l'application,
     * au lieu de naviguer vers l'activité précédente (LoginActivity).
     */
    // 💡 MISE EN PLACE DU GESTIONNAIRE DU BOUTON RETOUR MODERNE (OnBackPressedDispatcher)
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        // L'action est de fermer l'activité (et donc l'application)
        finish()
      }
    })

    /**
     * Collecte le HomeUiState du homeViewModel et met à jour l'UI en conséquence.
     */
    lifecycleScope.launch {
      homeViewModel.uiState.collectLatest { state ->

        // Gérer l'état de chargement
        loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        if (state.isLoading) {
          balance.text = getString(R.string.balance_loading)
          balance.visibility = View.VISIBLE
        }

        // 2. Gérer le solde (Succès)
        if (state.balance != null) {
          balance.text = formatBalance(state.balance)
          balance.visibility = View.VISIBLE
        }

        // 3. Gérer les erreurs
        if (state.error != null) {
          balance.visibility = View.GONE

          // Récupérer le message d'erreur à partir de l'ID de ressource
          val errorMessage = when (state.error) {
            R.string.error_user_not_found -> getString(
              R.string.error_user_not_found,
              homeViewModel.currentUserId
            )

            else -> getString(state.error)
          }
          Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_LONG).show()
        }
      }
    }
  }
    /**
     * Formate le Double en format monétaire français.
     */
  private fun formatBalance(balance: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    return format.format(balance)
  }



  override fun onCreateOptionsMenu(menu: Menu?): Boolean
  {
    menuInflater.inflate(R.menu.home_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean
  {
    return when (item.itemId)
    {
      R.id.disconnect ->
      {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
        true
      }
      else            -> super.onOptionsItemSelected(item)
    }
  }

}
