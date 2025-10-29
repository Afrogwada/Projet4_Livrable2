package com.aura.ui.home

import android.app.Activity
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
import com.aura.ui.model.formatBalance
import com.aura.ui.transfer.TransferActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The home activity for the app.
 */
class HomeActivity : AppCompatActivity() {
    // CONSTANTE pour l'ID utilisateur dans l'Intent
    companion object {
        const val EXTRA_USER_ID = "com.aura.ui.home.USER_ID"
    }

    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private var userId: String? = null

    // ---------------------------------------------------------------------------------------------
    // Initialisation et Gestion des Résultats d'Activités
    // ---------------------------------------------------------------------------------------------

    /** Callback pour le résultat de la TransferActivity. */
    private val startTransferActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleTransferActivityResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupInitialData() // Récupère l'ID et démarre le chargement
        setupListeners()
        setupBackButtonHandler()
        observeViewModel()
    }

    /**
     * Configure le View Binding et définit le content view.
     */
    private fun setupBinding() {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Récupère l'ID utilisateur de l'Intent et déclenche le chargement des comptes.
     * Redirige vers LoginActivity si l'ID est manquant.
     */
    private fun setupInitialData() {
        userId = intent.getStringExtra(EXTRA_USER_ID)

        if (!userId.isNullOrBlank()) {
            homeViewModel.loadUserAccounts(userId!!)
            binding.balance.text = "" // Initialisation
        } else {
            // Cas d'erreur : ID manquant
            navigateToLogin()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Listeners et Gestion des Événements
    // ---------------------------------------------------------------------------------------------

    /**
     * Configure le listener pour le bouton de virement.
     */
    private fun setupListeners() {
        binding.transfer.setOnClickListener {
            startTransferFlow()
        }
    }

    /**
     * Démarre la TransferActivity, si l'ID utilisateur est disponible.
     */
    private fun startTransferFlow() {
        if (userId != null) {
            val intent = Intent(this, TransferActivity::class.java).apply {
                putExtra(TransferActivity.EXTRA_USER_ID, userId)
            }
            startTransferActivityForResult.launch(intent)
        } else {
            Toast.makeText(this, "Erreur: ID utilisateur manquant.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Gère le résultat retourné par la TransferActivity.
     */
    private fun handleTransferActivityResult(result: ActivityResult) {
        // Recharger les comptes après un virement réussi pour mettre à jour le solde
        if (result.resultCode == Activity.RESULT_OK) {
            homeViewModel.refresh()
            // Optionnel: afficher un toast de succès
            Toast.makeText(this, getString(R.string.transfer_success), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configure le comportement du bouton de retour (Back).
     */
    private fun setupBackButtonHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // L'action est de fermer l'activité (et donc l'application)
                finish()
            }
        })
    }

    // ---------------------------------------------------------------------------------------------
    // Observation des Données (ViewModel)
    // ---------------------------------------------------------------------------------------------

    /**
     * Démarre la collecte du HomeUiState du ViewModel.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            homeViewModel.uiState.collectLatest { state ->
                handleLoadingState(state.isLoading)
                handleBalance(state.balance)
                handleError(state.error)
            }
        }
    }

    /**
     * Met à jour la visibilité de la barre de chargement et le texte du solde.
     */
    private fun handleLoadingState(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.balance.text = getString(R.string.balance_loading)
            binding.balance.visibility = View.VISIBLE
        }
    }

    /**
     * Met à jour le solde affiché en cas de succès.
     */
    private fun handleBalance(balance: Double?) {
        if (balance != null) {
            binding.balance.text = balance.formatBalance()
            binding.balance.visibility = View.VISIBLE
        }
    }

    /**
     * Gère l'affichage d'un message d'erreur si présent dans l'état.
     */
    private fun handleError(errorResId: Int?) {
        if (errorResId != null) {
            binding.balance.visibility = View.GONE

            // Construction du message d'erreur spécifique si nécessaire
            val errorMessage = when (errorResId) {
                R.string.error_user_not_found -> getString(
                    R.string.error_user_not_found,
                    homeViewModel.uiState.value.userId
                )

                else -> getString(errorResId)
            }
            Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Menu d'Options
    // ---------------------------------------------------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.disconnect -> {
                navigateToLogin()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Navigue vers l'écran de connexion et ferme cette activité.
     */
    private fun navigateToLogin() {
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }
}