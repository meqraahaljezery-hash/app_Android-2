package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// هذا الملف أصبح فارغاً لأن MaqraahViewModel هو المسؤول عن كل شيء الآن
class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun signInWithGoogle(email: String, name: String) {}
    fun signOut() {}
    fun updateProfile(name: String, preferredLanguage: String) {}
}