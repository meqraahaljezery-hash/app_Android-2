package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(
        // Default initialized to Admin Kalid for seamless preview & testing as requested
        User(
            id = "admin_kalid_01",
            name = "كالحة أحمد مصطفى (Kalid Ahmed)",
            email = "khdahd241@gmail.com",
            role = UserRole.ADMIN,
            minuteBalance = 500,
            tajweedLevel = "متقن للقراءات العشر بالسند",
            sanadTrack = "إجازة بالسند العالي في القراءات العشر"
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun signInWithGoogle(email: String, name: String) {
        val role = if (email == "khdahd241@gmail.com") UserRole.ADMIN else UserRole.STUDENT
        _currentUser.value = User(
            id = "user_${email.hashCode()}",
            name = name.ifBlank { "طالب المقرأة" },
            email = email,
            role = role,
            minuteBalance = 120,
            tajweedLevel = "متقن للمخارج والصفات",
            sanadTrack = "رواية حفص عن عاصم من طريق الشاطبية"
        )
    }

    fun signOut() {
        _currentUser.value = null
    }

    fun updateProfile(name: String, preferredLanguage: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(name = name)
    }
}
