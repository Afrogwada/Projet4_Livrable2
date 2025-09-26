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
import kotlinx.coroutines.delay
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

    // Observer le flow isLoginEnabled pour activer/désactiver le bouton
    lifecycleScope.launch {
      loginViewModel.isLoginEnabled.collectLatest { enabled ->
        login.isEnabled = enabled
      }
    }
    // Observer les champs et mettre à jour le ViewModel
    binding.identifier.addTextChangedListener { text ->
      loginViewModel.setIdentifier(text.toString())
    }
    binding.password.addTextChangedListener { text ->
      loginViewModel.setPassword(text.toString())
    }

    // Clic sur login
    login.setOnClickListener {
      loading.visibility = View.VISIBLE
      loginViewModel.login()

      // Observer le résultat du login
      lifecycleScope.launch {
        loginViewModel.loginResult.collectLatest { result ->
          //delay(10000L) // pour tester Manage the loading state on the login screen
          Logger.d("valeur de result: ${result.toString()}")
          loading.visibility = View.GONE
          if (result != null && result.granted) {
            // Identifiants corrects → ouvrir Home
            Logger.d("Identifiants corrects → ouvrir Home")
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
          } else if (!loginViewModel.networkError.value && result != null){
            // Message "Identifiants incorrects" seulement si pas d'erreur réseau
            Toast.makeText(this@LoginActivity, "Identifiants incorrects", Toast.LENGTH_LONG).show()
          }
        }
      }

      // Observer les erreurs réseau
      lifecycleScope.launch {
        loginViewModel.networkError.collectLatest { hasError ->
          if (hasError) {
            Toast.makeText(
              this@LoginActivity,
              "Impossible de se connecter : vérifiez votre connexion internet",
              Toast.LENGTH_LONG
            ).show()
          }
        }
      }
    }
  }

}
