/*
 * Author: Lei Tianyou
 * Role: Member D
 * Scope: Login and registration flow
 */

package com.example.wellnessapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var backToLoginButton: MaterialButton
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bindViews()
        bindActions()
        observeState()
    }

    private fun bindViews() {
        usernameInput = findViewById(R.id.registerUsernameInput)
        emailInput = findViewById(R.id.registerEmailInput)
        passwordInput = findViewById(R.id.registerPasswordInput)
        confirmPasswordInput = findViewById(R.id.registerConfirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)
        errorText = findViewById(R.id.registerErrorText)
        progressBar = findViewById(R.id.registerProgressBar)
    }

    private fun bindActions() {
        registerButton.setOnClickListener {
            viewModel.register(
                username = usernameInput.text?.toString().orEmpty(),
                email = emailInput.text?.toString().orEmpty(),
                password = passwordInput.text?.toString().orEmpty(),
                confirmPassword = confirmPasswordInput.text?.toString().orEmpty()
            )
        }

        backToLoginButton.setOnClickListener {
            openLogin()
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: UiState<*>) {
        when (state) {
            UiState.Idle,
            UiState.Empty -> setLoading(false)
            UiState.Loading -> {
                errorText.visibility = View.GONE
                setLoading(true)
            }
            is UiState.Success -> openLogin()
            is UiState.Error -> {
                setLoading(false)
                errorText.text = state.message
                errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerButton.isEnabled = !isLoading
        backToLoginButton.isEnabled = !isLoading
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}
