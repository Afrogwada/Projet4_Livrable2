package com.aura.ui.login

import androidx.activity.viewModels
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.aura.databinding.ActivityLoginBinding
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

    login.setOnClickListener {
      loading.visibility = View.VISIBLE

      val intent = Intent(this@LoginActivity, HomeActivity::class.java)
      startActivity(intent)

      finish()
    }
  }

}
