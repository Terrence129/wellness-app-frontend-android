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
import com.example.wellnessapp.ui.home.HomeActivity
import com.example.wellnessapp.util.UiState
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindViews()
        bindActions()
        observeState()
    }

    private fun bindViews() {
        emailInput = findViewById(R.id.loginEmailInput)
        passwordInput = findViewById(R.id.loginPasswordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.goToRegisterButton)
        errorText = findViewById(R.id.loginErrorText)
        progressBar = findViewById(R.id.loginProgressBar)
    }

    private fun bindActions() {
        loginButton.setOnClickListener {
            viewModel.login(
                email = emailInput.text?.toString().orEmpty(),
                password = passwordInput.text?.toString().orEmpty()
            )
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
            is UiState.Success -> openHome()
            is UiState.Error -> {
                setLoading(false)
                errorText.text = state.message
                errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !isLoading
        registerButton.isEnabled = !isLoading
    }

    private fun openHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
