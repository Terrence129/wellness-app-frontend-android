// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.local.TokenManager
import com.example.wellnessapp.data.model.PersonalInfoResponse
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.repository.UserRepository
import com.example.wellnessapp.ui.auth.LoginActivity
import com.example.wellnessapp.ui.history.HistoryActivity
import com.example.wellnessapp.ui.log.AddWellnessLogActivity
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem
import java.util.Locale
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val userRepository by lazy { UserRepository(applicationContext) }

    private var currentUser: UserResponse? = null
    private var currentPersonalInfo: PersonalInfoResponse? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var bodyStatusText: TextView
    private lateinit var heightText: TextView
    private lateinit var weightText: TextView
    private lateinit var bmiText: TextView
    private lateinit var activityText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bindViews()
        bindActions()
        BottomNavigationController.attach(this, ActiveItem.PROFILE)
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressProfile)
        errorText = findViewById(R.id.tvProfileError)
        nameText = findViewById(R.id.tvProfileName)
        emailText = findViewById(R.id.tvProfileEmail)
        bodyStatusText = findViewById(R.id.tvBodyStatus)
        heightText = findViewById(R.id.tvProfileHeight)
        weightText = findViewById(R.id.tvProfileWeight)
        bmiText = findViewById(R.id.tvProfileBmi)
        activityText = findViewById(R.id.tvProfileActivity)
    }

    private fun bindActions() {
        findViewById<View>(R.id.btnProfileBack).setOnClickListener { finish() }
        findViewById<View>(R.id.cardAccount).setOnClickListener { showAccountDetails() }
        findViewById<View>(R.id.btnEditBodyProfile).setOnClickListener {
            startActivity(
                Intent(this, AddWellnessLogActivity::class.java)
                    .putExtra(AddWellnessLogActivity.EXTRA_EDIT_BODY_PROFILE, true)
            )
        }
        findViewById<View>(R.id.cardWellnessData).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<View>(R.id.cardLogout).setOnClickListener { logout() }
    }

    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val userResponse = userRepository.getCurrentUser()
                if (!userResponse.success || userResponse.data == null) {
                    showError(userResponse.message.ifBlank { "Unable to load profile." })
                    return@launch
                }

                currentUser = userResponse.data
                currentPersonalInfo = runCatching {
                    userRepository.getPersonalInfo()
                }.getOrNull()?.takeIf { it.success }?.data
                showProfile(userResponse.data, currentPersonalInfo)
            } catch (error: Exception) {
                showError(error.message ?: "Unable to load profile.")
            }
        }
    }

    private fun showProfile(user: UserResponse, info: PersonalInfoResponse?) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE

        nameText.text = user.username
        emailText.text = user.email

        val height = info?.heightCm?.takeIf { it > 0.0 } ?: user.heightCm
        val weight = info?.weightKg?.takeIf { it > 0.0 } ?: user.weightKg
        val bmi = info?.bmi ?: calculateBmi(height, weight)

        bodyStatusText.text = if (height != null && weight != null) "Ready" else "Incomplete"
        heightText.text = height?.let { "Height ${formatDecimal(it)} cm" } ?: "Height --"
        weightText.text = weight?.let { "Weight ${formatDecimal(it)} kg" } ?: "Weight --"
        bmiText.text = bmi?.let { String.format(Locale.US, "BMI %.1f", it) } ?: "BMI --"
        activityText.text = info?.activityLevel?.takeIf { it.isNotBlank() } ?: "Activity --"
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun showAccountDetails() {
        val user = currentUser ?: return
        val info = currentPersonalInfo
        AlertDialog.Builder(this)
            .setTitle(user.username)
            .setMessage(
                listOfNotNull(
                    "Email: ${user.email}",
                    info?.gender?.takeIf { it.isNotBlank() }?.let { "Gender: $it" },
                    info?.dateOfBirth?.takeIf { it.isNotBlank() }?.let { "Date of birth: $it" }
                ).joinToString("\n")
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun logout() {
        lifecycleScope.launch {
            TokenManager(this@ProfileActivity).clearToken()
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun calculateBmi(heightCm: Double?, weightKg: Double?): Double? {
        if (heightCm == null || weightKg == null || heightCm <= 0.0 || weightKg <= 0.0) return null
        val heightMeter = heightCm / 100.0
        return weightKg / (heightMeter * heightMeter)
    }

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }
}
