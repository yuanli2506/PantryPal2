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

sealed class RegisterResult {
    data class Success(val user: User) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
    object Loading : RegisterResult()
}

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerResult.value = RegisterResult.Loading
            try {
                val user = User(name = name, email = email, password = password)
                val result = userRepository.register(user)

                result.onSuccess { userId ->
                    preferenceManager.setLoggedIn(true)
                    preferenceManager.saveUserId(userId.toInt())
                    preferenceManager.setUserName(name)
                    preferenceManager.setUserEmail(email)
                    _registerResult.value = RegisterResult.Success(user)
                }.onFailure { exception ->
                    _registerResult.value = RegisterResult.Error(exception.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerResult.value = RegisterResult.Error("Registration failed: ${e.message}")
            }
        }
    }
}

class RegisterViewModelFactory(
    private val userRepository: UserRepository,
    private val preferenceManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(userRepository, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
