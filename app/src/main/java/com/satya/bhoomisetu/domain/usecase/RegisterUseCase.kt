package com.satya.bhoomisetu.domain.usecase

import com.satya.bhoomisetu.domain.model.SurveyorProfile
import com.satya.bhoomisetu.domain.model.User
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.util.Result
import com.satya.bhoomisetu.util.Validators
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: UserRole,
        licenseNumber: String = "",
        experienceYears: String = "",
        serviceAreas: List<String> = emptyList()
    ): Result<String> {
        if (!Validators.isValidName(name)) {
            return Result.Error(IllegalArgumentException("Name must be at least 2 characters."))
        }
        if (!Validators.isValidPhone(phone)) {
            return Result.Error(IllegalArgumentException("Please enter a valid 10-digit Indian mobile number (starting with 6, 7, 8, or 9)."))
        }
        if (!Validators.isValidEmail(email)) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address."))
        }
        if (!Validators.isValidPassword(password)) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (!Validators.doPasswordsMatch(password, confirmPassword)) {
            return Result.Error(IllegalArgumentException("Passwords do not match."))
        }

        var surveyorProfile: SurveyorProfile? = null
        var legalAdvisorProfile: com.satya.bhoomisetu.domain.model.LegalAdvisorProfile? = null
        var documentWriterProfile: com.satya.bhoomisetu.domain.model.DocumentWriterProfile? = null

        if (role == UserRole.SURVEYOR) {
            if (!Validators.isValidLicenseNumber(licenseNumber)) {
                return Result.Error(IllegalArgumentException("License number is required for surveyors."))
            }
            if (!Validators.isValidExperienceYears(experienceYears)) {
                return Result.Error(IllegalArgumentException("Years of experience must be a valid number (0-50)."))
            }
            if (serviceAreas.isEmpty()) {
                return Result.Error(IllegalArgumentException("Please select at least one service area district."))
            }

            surveyorProfile = SurveyorProfile(
                userId = "",
                licenseNumber = licenseNumber.trim(),
                serviceAreas = serviceAreas,
                yearsOfExperience = experienceYears.toIntOrNull() ?: 0
            )
        } else if (role == UserRole.LEGAL_ADVISOR) {
            if (licenseNumber.trim().isBlank()) {
                return Result.Error(IllegalArgumentException("Bar Council / Legal Advisor enrollment number is required."))
            }
            if (serviceAreas.isEmpty()) {
                return Result.Error(IllegalArgumentException("Please select at least one practice district in Andhra Pradesh."))
            }

            legalAdvisorProfile = com.satya.bhoomisetu.domain.model.LegalAdvisorProfile(
                enrollmentNumber = licenseNumber.trim(),
                districts = serviceAreas,
                yearsOfExperience = experienceYears.toIntOrNull() ?: 0
            )
        } else if (role == UserRole.DOCUMENT_WRITER) {
            if (serviceAreas.isEmpty()) {
                return Result.Error(IllegalArgumentException("Please select at least one service district in Andhra Pradesh."))
            }

            documentWriterProfile = com.satya.bhoomisetu.domain.model.DocumentWriterProfile(
                licenseNumber = licenseNumber.trim(),
                districts = serviceAreas,
                yearsOfExperience = experienceYears.toIntOrNull() ?: 0
            )
        }

        return authRepository.register(
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim(),
            password = password,
            role = role,
            surveyorProfile = surveyorProfile,
            legalAdvisorProfile = legalAdvisorProfile,
            documentWriterProfile = documentWriterProfile
        )
    }
}

