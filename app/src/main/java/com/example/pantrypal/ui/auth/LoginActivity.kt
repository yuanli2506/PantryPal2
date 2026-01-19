package com.example.pantrypal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.databinding.ActivityLoginBinding
import com.example.pantrypal.data.repository.UserRepository
import com.example.pantrypal.ui.MainActivity
import com.example.pantrypal.util.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepository: UserRepository
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val database = (application as PantryPalApplication).database
        userRepository = UserRepository(database.userDao())
        preferenceManager = PreferenceManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Register link
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Skip login (for demo purposes)
        binding.tvSkip.setOnClickListener {
            preferenceManager.setLoggedIn(true)
            preferenceManager.saveUserId(-1) // Guest user
            preferenceManager.saveUserName("Guest")
            navigateToMain()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val user = userRepository.login(email, password)
                
                if (user != null) {
                    // Save user session
                    preferenceManager.setLoggedIn(true)
                    preferenceManager.saveUserId(user.id)
                    preferenceManager.saveUserName(user.name)  // ← Gets latest name from DB
                    preferenceManager.saveUserEmail(user.email)
                    preferenceManager.saveProfilePicture(user.profilePicture)  //
                    
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
