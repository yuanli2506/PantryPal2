package com.example.pantrypal.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pantrypal.data.model.User
import com.example.pantrypal.data.repository.UserRepository
import com.example.pantrypal.util.PreferenceManager
import kotlinx.coroutines.launch

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object Loading : LoginResult()
}

class LoginViewModel(
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = LoginResult.Loading
            try {
                val user = userRepository.login(email, password)
                if (user != null) {
                    // Save session
                    preferenceManager.setLoggedIn(true)
                    preferenceManager.saveUserId(user.id)
                    preferenceManager.setUserName(user.name)
                    preferenceManager.setUserEmail(user.email)
                    _loginResult.value = LoginResult.Success(user)
                } else {
                    _loginResult.value = LoginResult.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error("Login failed: ${e.message}")
            }
        }
    }

    fun skipLogin() {
        preferenceManager.setLoggedIn(true)
        preferenceManager.saveUserId(-1) // Guest user
        preferenceManager.setUserName("Guest")
        // Create a temporary guest user for the result
        val guestUser = User(id = -1, name = "Guest", email = "", password = "")
        _loginResult.value = LoginResult.Success(guestUser)
    }
}

class LoginViewModelFactory(
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
