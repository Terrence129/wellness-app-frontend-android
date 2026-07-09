// Team5
// Author: Chen Chen
package com.example.wellnessapp.ui.log

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.PersonalInfoResponse
import com.example.wellnessapp.data.model.PersonalInfoUpsertRequest
import com.example.wellnessapp.data.repository.UserRepository
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem
import com.example.wellnessapp.util.ErrorMessageMapper
import com.example.wellnessapp.util.ValidationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch
import retrofit2.HttpException

class `AddWellnessLogActivity` : AppCompatActivity() {

    private val viewModel: AddWellnessLogViewModel by viewModels()
    private val userRepository by lazy { UserRepository(applicationContext) }

    private lateinit var profilePrefs: SharedPreferences
    private lateinit var bodyFormContent: LinearLayout
    private lateinit var bodySummaryRow: LinearLayout
    private lateinit var dailyLogSection: LinearLayout
    private lateinit var inputHeightCm: EditText
    private lateinit var inputWeightKg: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var inputDateOfBirth: EditText
    private lateinit var buttonPickDateOfBirth: Button
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var buttonContinueToLog: Button
    private lateinit var buttonEditBodyProfile: Button
    private lateinit var textBodySummary: TextView
    private lateinit var textStepHint: TextView
    private lateinit var stepBodyCircle: TextView
    private lateinit var stepBodyText: TextView
    private lateinit var stepLogCircle: TextView
    private lateinit var stepLogText: TextView
    private lateinit var inputLogDate: EditText
    private lateinit var buttonPickDate: Button
    private lateinit var inputSleepHours: EditText
    private lateinit var spinnerMoodScore: Spinner
    private lateinit var inputWaterCups: EditText
    private lateinit var inputSteps: EditText
    private lateinit var inputExerciseMinutes: EditText
    private lateinit var inputNote: EditText
    private lateinit var buttonSubmitLog: Button
    private lateinit var textError: TextView
    private lateinit var progressBar: ProgressBar

    private val genderOptions = listOf(
        SelectionOption("Select gender", ""),
        SelectionOption("Male", "MALE"),
        SelectionOption("Female", "FEMALE"),
        SelectionOption("Non-binary", "NON_BINARY"),
        SelectionOption("Prefer not to say", "PREFER_NOT_TO_SAY")
    )
    private val activityLevelOptions = listOf(
        SelectionOption("Select activity level", ""),
        SelectionOption("Sedentary", "SEDENTARY"),
        SelectionOption("Lightly active", "LIGHTLY_ACTIVE"),
        SelectionOption("Moderately active", "MODERATELY_ACTIVE"),
        SelectionOption("Very active", "VERY_ACTIVE")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_wellness_log)

        profilePrefs = getSharedPreferences(PROFILE_PREFS_NAME, MODE_PRIVATE)
        bindViews()
        setupSpinners()
        setupInitialDate()
        setupInitialStep()
        setupListeners()
        BottomNavigationController.attach(this, ActiveItem.ADD)
        observeState()
    }

    private fun bindViews() {
        bodyFormContent = findViewById(R.id.bodyFormContent)
        bodySummaryRow = findViewById(R.id.bodySummaryRow)
        dailyLogSection = findViewById(R.id.dailyLogSection)
        inputHeightCm = findViewById(R.id.inputHeightCm)
        inputWeightKg = findViewById(R.id.inputWeightKg)
        spinnerGender = findViewById(R.id.spinnerGender)
        inputDateOfBirth = findViewById(R.id.inputDateOfBirth)
        buttonPickDateOfBirth = findViewById(R.id.buttonPickDateOfBirth)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        buttonContinueToLog = findViewById(R.id.buttonContinueToLog)
        buttonEditBodyProfile = findViewById(R.id.buttonEditBodyProfile)
        textBodySummary = findViewById(R.id.textBodySummary)
        textStepHint = findViewById(R.id.textStepHint)
        stepBodyCircle = findViewById(R.id.stepBodyCircle)
        stepBodyText = findViewById(R.id.stepBodyText)
        stepLogCircle = findViewById(R.id.stepLogCircle)
        stepLogText = findViewById(R.id.stepLogText)
        inputLogDate = findViewById(R.id.inputLogDate)
        buttonPickDate = findViewById(R.id.buttonPickDate)
        inputSleepHours = findViewById(R.id.inputSleepHours)
        spinnerMoodScore = findViewById(R.id.spinnerMoodScore)
        inputWaterCups = findViewById(R.id.inputWaterCups)
        inputSteps = findViewById(R.id.inputSteps)
        inputExerciseMinutes = findViewById(R.id.inputExerciseMinutes)
        inputNote = findViewById(R.id.inputNote)
        buttonSubmitLog = findViewById(R.id.buttonSubmitLog)
        textError = findViewById(R.id.textError)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinners() {
        spinnerMoodScore.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("1", "2", "3", "4", "5")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerMoodScore.setSelection(2)

        spinnerGender.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            genderOptions.map { it.label }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerActivityLevel.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            activityLevelOptions.map { it.label }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupInitialDate() {
        inputLogDate.setText(currentDate())
    }

    private fun setupInitialStep() {
        getSavedBodyProfile()?.let { profile ->
            bindBodyProfileInputs(profile)
            showDailyLogStep(profile)
        } ?: showBodyProfileStep()
        loadBodyProfileFromBackend()
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        buttonPickDate.setOnClickListener { showLogDatePicker() }
        inputLogDate.setOnClickListener { showLogDatePicker() }
        buttonPickDateOfBirth.setOnClickListener { showBirthDatePicker() }
        inputDateOfBirth.setOnClickListener { showBirthDatePicker() }

        buttonContinueToLog.setOnClickListener {
            val profile = validateBodyProfile() ?: return@setOnClickListener
            submitBodyProfile(profile)
        }
        buttonEditBodyProfile.setOnClickListener {
            getSavedBodyProfile()?.let { profile ->
                bindBodyProfileInputs(profile)
            }
            showBodyProfileStep()
        }

        buttonSubmitLog.setOnClickListener {
            viewModel.createLog(
                logDate = inputLogDate.text.toString(),
                sleepHoursText = inputSleepHours.text.toString(),
                moodScoreText = spinnerMoodScore.selectedItem.toString(),
                waterCupsText = inputWaterCups.text.toString(),
                stepsText = inputSteps.text.toString(),
                exerciseMinutesText = inputExerciseMinutes.text.toString(),
                note = inputNote.text.toString()
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AddLogUiState.Idle -> {
                    setLoading(false)
                    textError.visibility = View.GONE
                }

                is AddLogUiState.Loading -> {
                    setLoading(true)
                    textError.visibility = View.GONE
                }

                is AddLogUiState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Wellness log saved", Toast.LENGTH_SHORT).show()
                    finish()
                }

                is AddLogUiState.ConfirmOverwrite -> {
                    setLoading(false)
                    textError.visibility = View.GONE
                    showOverwriteDialog(state)
                }

                is AddLogUiState.Error -> {
                    setLoading(false)
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showLogDatePicker() {
        showDatePicker(
            target = inputLogDate,
            maxDate = Calendar.getInstance().timeInMillis
        )
    }

    private fun showBirthDatePicker() {
        showDatePicker(
            target = inputDateOfBirth,
            maxDate = Calendar.getInstance().timeInMillis - ONE_DAY_MILLIS,
            dialogTheme = R.style.WellnessBirthDatePickerDialog
        )
    }

    private fun showDatePicker(target: EditText, maxDate: Long, dialogTheme: Int = 0) {
        val calendar = Calendar.getInstance()
        parseExistingDate(target.text.toString())?.let {
            calendar.set(it[0], it[1] - 1, it[2])
        }

        DatePickerDialog(
            this,
            dialogTheme,
            { _, year, month, dayOfMonth ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                target.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = maxDate
        }.show()
    }

    private fun validateBodyProfile(): BodyProfile? {
        val heightText = inputHeightCm.text.toString().trim()
        val weightText = inputWeightKg.text.toString().trim()
        val gender = genderOptions.getOrNull(spinnerGender.selectedItemPosition)?.value.orEmpty()
        val dateOfBirth = inputDateOfBirth.text.toString().trim()
        val activityLevel = activityLevelOptions.getOrNull(spinnerActivityLevel.selectedItemPosition)?.value.orEmpty()

        val validationMessage = ValidationUtils.validatePersonalInfo(
            heightCm = heightText,
            weightKg = weightText,
            gender = gender,
            dateOfBirth = dateOfBirth,
            activityLevel = activityLevel,
            today = currentDate()
        )
        if (validationMessage != null) {
            showInlineError(validationMessage)
            return null
        }

        textError.visibility = View.GONE
        return BodyProfile(
            heightCm = heightText.toDouble(),
            weightKg = weightText.toDouble(),
            gender = gender,
            dateOfBirth = dateOfBirth,
            activityLevel = activityLevel
        )
    }

    private fun submitBodyProfile(profile: BodyProfile) {
        lifecycleScope.launch {
            setLoading(true)
            textError.visibility = View.GONE

            runCatching {
                userRepository.upsertPersonalInfo(profile.toRequest())
            }.onSuccess { response ->
                val personalInfo = response.data
                if (response.success && personalInfo != null) {
                    val savedProfile = BodyProfile.fromResponse(personalInfo)
                    saveBodyProfile(savedProfile)
                    bindBodyProfileInputs(savedProfile)
                    showDailyLogStep(savedProfile)
                } else {
                    showInlineError(
                        ErrorMessageMapper.fromBackendMessage(
                            response.message,
                            "Unable to save your body profile."
                        )
                    )
                }
            }.onFailure { throwable ->
                showInlineError(
                    ErrorMessageMapper.fromThrowable(
                        throwable,
                        "Unable to save your body profile."
                    )
                )
            }

            setLoading(false)
        }
    }

    private fun loadBodyProfileFromBackend() {
        lifecycleScope.launch {
            runCatching {
                userRepository.getPersonalInfo()
            }.onSuccess { response ->
                val personalInfo = response.data
                if (response.success && personalInfo != null) {
                    val profile = BodyProfile.fromResponse(personalInfo)
                    saveBodyProfile(profile)
                    bindBodyProfileInputs(profile)
                    showDailyLogStep(profile)
                }
            }.onFailure { throwable ->
                if (throwable is HttpException && throwable.code() == 404) {
                    return@onFailure
                }
            }
        }
    }

    private fun bindBodyProfileInputs(profile: BodyProfile) {
        inputHeightCm.setText(profile.heightCm.formatForInput())
        inputWeightKg.setText(profile.weightKg.formatForInput())
        inputDateOfBirth.setText(profile.dateOfBirth)
        spinnerGender.setSelection(genderOptions.indexOfFirst { it.value == profile.gender }.coerceAtLeast(0))
        spinnerActivityLevel.setSelection(
            activityLevelOptions.indexOfFirst { it.value == profile.activityLevel }.coerceAtLeast(0)
        )
    }

    private fun saveBodyProfile(profile: BodyProfile) {
        profilePrefs.edit()
            .putFloat(KEY_HEIGHT_CM, profile.heightCm.toFloat())
            .putFloat(KEY_WEIGHT_KG, profile.weightKg.toFloat())
            .putString(KEY_GENDER, profile.gender)
            .putString(KEY_DATE_OF_BIRTH, profile.dateOfBirth)
            .putString(KEY_ACTIVITY_LEVEL, profile.activityLevel)
            .apply()
    }

    private fun getSavedBodyProfile(): BodyProfile? {
        if (!profilePrefs.contains(KEY_HEIGHT_CM) ||
            !profilePrefs.contains(KEY_WEIGHT_KG) ||
            !profilePrefs.contains(KEY_GENDER) ||
            !profilePrefs.contains(KEY_DATE_OF_BIRTH) ||
            !profilePrefs.contains(KEY_ACTIVITY_LEVEL)
        ) {
            return null
        }
        return BodyProfile(
            heightCm = profilePrefs.getFloat(KEY_HEIGHT_CM, 0f).toDouble(),
            weightKg = profilePrefs.getFloat(KEY_WEIGHT_KG, 0f).toDouble(),
            gender = profilePrefs.getString(KEY_GENDER, "").orEmpty(),
            dateOfBirth = profilePrefs.getString(KEY_DATE_OF_BIRTH, "").orEmpty(),
            activityLevel = profilePrefs.getString(KEY_ACTIVITY_LEVEL, "").orEmpty()
        )
    }

    private fun showBodyProfileStep() {
        bodyFormContent.visibility = View.VISIBLE
        bodySummaryRow.visibility = View.GONE
        dailyLogSection.visibility = View.GONE
        textStepHint.text = "Start with your body profile, then add today's log."
        buttonSubmitLog.isEnabled = false
        updateStepIndicator(isDailyStep = false)
        textError.visibility = View.GONE
    }

    private fun showDailyLogStep(profile: BodyProfile) {
        updateBodySummary(profile)
        bodyFormContent.visibility = View.GONE
        bodySummaryRow.visibility = View.VISIBLE
        dailyLogSection.visibility = View.VISIBLE
        textStepHint.text = "Body profile saved. Add the signals that changed today."
        buttonSubmitLog.isEnabled = true
        updateStepIndicator(isDailyStep = true)
        textError.visibility = View.GONE
    }

    private fun updateBodySummary(profile: BodyProfile) {
        val heightMeters = profile.heightCm / 100.0
        val bmi = profile.weightKg / (heightMeters * heightMeters)
        textBodySummary.text =
            "Height ${profile.heightCm.formatForInput()} cm / Weight ${profile.weightKg.formatForInput()} kg / BMI ${String.format(Locale.US, "%.1f", bmi)} / DOB ${profile.dateOfBirth}"
    }

    private fun updateStepIndicator(isDailyStep: Boolean) {
        val activeColor = getColor(R.color.function_button_background)
        val completeColor = getColor(R.color.health_mint)
        val inactiveColor = getColor(R.color.classic_gray)
        val inactiveTextColor = getColor(R.color.health_text_secondary)
        val primaryTextColor = getColor(R.color.health_text_primary)

        stepBodyCircle.background = circleDrawable(if (isDailyStep) completeColor else activeColor)
        stepBodyCircle.setTextColor(getColor(R.color.white))
        stepBodyText.setTextColor(primaryTextColor)

        stepLogCircle.background = circleDrawable(if (isDailyStep) activeColor else inactiveColor)
        stepLogCircle.setTextColor(if (isDailyStep) getColor(R.color.white) else inactiveTextColor)
        stepLogText.setTextColor(if (isDailyStep) primaryTextColor else inactiveTextColor)
    }

    private fun circleDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun showInlineError(message: String) {
        textError.text = message
        textError.visibility = View.VISIBLE
    }

    private fun showOverwriteDialog(state: AddLogUiState.ConfirmOverwrite) {
        AlertDialog.Builder(this)
            .setTitle("Overwrite existing log?")
            .setMessage("A wellness log already exists for ${state.request.logDate}. Do you want to overwrite it?")
            .setPositiveButton("Overwrite") { _, _ ->
                viewModel.overwriteLog(state.existingLog, state.request)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.cancelOverwrite()
            }
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonSubmitLog.isEnabled = !isLoading
        buttonPickDate.isEnabled = !isLoading
        buttonPickDateOfBirth.isEnabled = !isLoading
        buttonContinueToLog.isEnabled = !isLoading
        buttonEditBodyProfile.isEnabled = !isLoading
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
    }

    private fun parseExistingDate(value: String): List<Int>? {
        val parts = value.trim().split("-")
        if (parts.size != 3) {
            return null
        }
        return parts.mapNotNull { it.toIntOrNull() }.takeIf { it.size == 3 }
    }

    private fun Double.formatForInput(): String {
        return if (this % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", this)
        } else {
            String.format(Locale.US, "%.1f", this)
        }
    }

    private data class BodyProfile(
        val heightCm: Double,
        val weightKg: Double,
        val gender: String,
        val dateOfBirth: String,
        val activityLevel: String
    ) {
        fun toRequest(): PersonalInfoUpsertRequest {
            return PersonalInfoUpsertRequest(
                heightCm = heightCm,
                weightKg = weightKg,
                gender = gender,
                dateOfBirth = dateOfBirth,
                activityLevel = activityLevel
            )
        }

        companion object {
            fun fromResponse(response: PersonalInfoResponse): BodyProfile {
                return BodyProfile(
                    heightCm = response.heightCm,
                    weightKg = response.weightKg,
                    gender = response.gender,
                    dateOfBirth = response.dateOfBirth.orEmpty(),
                    activityLevel = response.activityLevel
                )
            }
        }
    }

    private data class SelectionOption(
        val label: String,
        val value: String
    )

    private companion object {
        const val PROFILE_PREFS_NAME = "wellness_body_profile"
        const val KEY_HEIGHT_CM = "height_cm"
        const val KEY_WEIGHT_KG = "weight_kg"
        const val KEY_GENDER = "gender"
        const val KEY_DATE_OF_BIRTH = "date_of_birth"
        const val KEY_ACTIVITY_LEVEL = "activity_level"
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
