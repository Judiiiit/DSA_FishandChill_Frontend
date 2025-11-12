package com.example.android_proyecto
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.android_proyecto.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope


class
AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableLiveData<Result<User>>()
    val loginState: LiveData<Result<User>> = _loginState

    private val _registerState = MutableLiveData<Result<User>>()
    val registerState: LiveData<Result<User>> = _registerState

    fun doLogin(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Elige la variante que corresponda a tu backend:
            val result = repo.loginWithEndpoint(username, password)
            // val result = repo.loginByUsername(username, password)
            // val result = repo.loginFromList(username, password)
            _loginState.postValue(result)
        }
    }

    fun doRegister(username: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.register(username, email, password)
            _registerState.postValue(result)
        }
    }
}
