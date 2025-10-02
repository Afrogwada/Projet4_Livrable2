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
class LoginActivity : AppCompatActivity()
{

  /** Binding généré automatiquement pour accéder aux vues du layout XML. */
  private lateinit var binding: ActivityLoginBinding

  /** ViewModel associé à cet écran. */
  private val loginViewModel: LoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)

    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val login = binding.login
    val loading = binding.loading
    val identifier = binding.identifier
    val password = binding.password

    // Observer le flow isLoginEnabled pour activer/désactiver le bouton
    lifecycleScope.launch {
      loginViewModel.isLoginEnabled.collectLatest { enabled ->
        login.isEnabled = enabled
      }
    }

    // Observer les champs et mettre à jour le ViewModel
    identifier.addTextChangedListener { text ->
      loginViewModel.setIdentifier(text.toString())
    }
    password.addTextChangedListener { text ->
      loginViewModel.setPassword(text.toString())
    }

    // Clic sur login
    login.setOnClickListener {
      loginViewModel.login()
    }

    // ---------------------------------------------------------------------------------------------
    // NOUVEL OBSERVATEUR UNIQUE DU CONTENEUR D'ÉTAT (uiState)
    // ---------------------------------------------------------------------------------------------
    lifecycleScope.launch {
      loginViewModel.uiState.collectLatest { uiState ->
        Logger.d("Nouvel état UI : $uiState")

        // Gérer l'état de chargement
        loading.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
        login.isEnabled = !uiState.isLoading // Désactiver le bouton pendant le chargement

        //  Gérer le succès
        if (uiState.isSuccess == true) {
          // Connexion réussie → ouvrir Home
          Logger.d("Connexion réussie → ouvrir Home")
          val intent = Intent(this@LoginActivity, HomeActivity::class.java)
          startActivity(intent)
          finish() //Ferme le LoginActivity, Cela empêche l’utilisateur de revenir en arrière sur l’écran de login en appuyant sur la touche Back.
        }
        // 3. Gérer l'échec (avec ou sans message d'erreur)
        else if (uiState.isSuccess == false && uiState.errorMessage != null) {
          // Afficher le message d'erreur provenant du ViewModel
          Toast.makeText(this@LoginActivity, uiState.errorMessage, Toast.LENGTH_LONG).show()
        }
      }
    }
    // ---------------------------------------------------------------------------------------------

    // L'ancien bloc d'observateurs de loginResult et networkError est supprimé
  }
}