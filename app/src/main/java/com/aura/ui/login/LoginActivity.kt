package com.aura.ui.login

import androidx.activity.viewModels
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityLoginBinding
import com.aura.ui.Logger
import com.aura.ui.home.HomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * The login activity for the app.
 */
class LoginActivity : AppCompatActivity() {

    /** Binding généré automatiquement pour accéder aux vues du layout XML. */
    private lateinit var binding: ActivityLoginBinding

    /** ViewModel associé à cet écran. */
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupInputListeners()
        setupLoginButton()
        observeViewModel()
    }

    /**
     * Configure le View Binding et définit le content view.
     */
    private fun setupBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Met en place les écouteurs de texte pour les champs d'identifiant et de mot de passe.
     */
    private fun setupInputListeners() {
        // Observer les champs et mettre à jour le ViewModel
        binding.identifier.addTextChangedListener { text ->
            loginViewModel.setIdentifier(text.toString())
        }
        binding.password.addTextChangedListener { text ->
            loginViewModel.setPassword(text.toString())
        }
    }

    /**
     * Configure l'action du bouton de connexion.
     */
    private fun setupLoginButton() {
        // Clic sur login
        binding.login.setOnClickListener {
            loginViewModel.login()
        }
    }

    /**
     * Démarre l'observation des LiveData/StateFlow du ViewModel.
     */
    private fun observeViewModel() {
        // Observer le flow isLoginEnabled pour activer/désactiver le bouton
        lifecycleScope.launch {
            loginViewModel.isLoginEnabled.collectLatest { enabled ->
                binding.login.isEnabled = enabled
            }
        }

        // NOUVEL OBSERVATEUR UNIQUE DU CONTENEUR D'ÉTAT (uiState)
        lifecycleScope.launch {
            loginViewModel.uiState.collectLatest { uiState ->
                Logger.d("Nouvel état UI : $uiState")
                handleLoading(uiState.isLoading)
                handleLoginResult(uiState)
            }
        }
    }

    /**
     * Gère la visibilité de la barre de chargement.
     * @param isLoading L'état actuel de chargement.
     */
    private fun handleLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Gère le résultat de la tentative de connexion (succès ou échec).
     * @param uiState L'état actuel de l'UI.
     */
    private fun handleLoginResult(uiState: LoginUiState) {
        // Gérer le succès
        if (uiState.isSuccess == true) {
            // Connexion réussie → ouvrir Home
            Logger.d("Connexion réussie → ouvrir Home")
            navigateToHome(uiState.userId)
        }
        // Gérer l'échec (avec message d'erreur)
        else if (uiState.isSuccess == false && uiState.error != null) {
            // Afficher le message d'erreur provenant du ViewModel
            Toast.makeText(this, getString(uiState.error), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Ouvre la HomeActivity et ferme la LoginActivity.
     * @param userId L'ID de l'utilisateur connecté.
     */
    private fun navigateToHome(userId: String?) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            // Ajouter l'ID utilisateur comme extra
            putExtra(HomeActivity.EXTRA_USER_ID, userId)
        }
        startActivity(intent)
        finish() // Ferme le LoginActivity pour empêcher l'utilisateur de revenir en arrière.
    }
}
