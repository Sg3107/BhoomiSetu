package com.satya.bhoomisetu.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.presentation.components.AppTextField
import com.satya.bhoomisetu.presentation.components.ErrorBanner
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Constants
import com.satya.bhoomisetu.util.localization.LanguageManager
import com.satya.bhoomisetu.util.localization.LanguageSwitchAction
import com.satya.bhoomisetu.util.localization.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    languageManager: LanguageManager,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    val fieldsState by viewModel.fieldsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.paddingLarge)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Language switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSwitchAction(
                languageManager = languageManager,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        Text(
            text = strings.createAccount,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = strings.registerSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        // Role Selection Grid (4 roles: Citizen, Surveyor, Legal Advisor, Document Writer)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = fieldsState.selectedRole == UserRole.USER,
                    onClick = { viewModel.onRoleChanged(UserRole.USER) },
                    label = { Text(strings.roleCitizen, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = fieldsState.selectedRole == UserRole.SURVEYOR,
                    onClick = { viewModel.onRoleChanged(UserRole.SURVEYOR) },
                    label = { Text(strings.roleSurveyor, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = fieldsState.selectedRole == UserRole.LEGAL_ADVISOR,
                    onClick = { viewModel.onRoleChanged(UserRole.LEGAL_ADVISOR) },
                    label = { Text(strings.roleLegalAdvisor, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = fieldsState.selectedRole == UserRole.DOCUMENT_WRITER,
                    onClick = { viewModel.onRoleChanged(UserRole.DOCUMENT_WRITER) },
                    label = { Text(strings.roleDocumentWriter, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        uiState.errorMessage?.let { error ->
            ErrorBanner(message = error)
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        }

        // Full Name
        AppTextField(
            value = fieldsState.name,
            onValueChange = viewModel::onNameChanged,
            label = strings.fullName,
            leadingIcon = Icons.Default.Person,
            errorText = fieldsState.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Mobile Phone (Strict Indian 10-digit mobile)
        AppTextField(
            value = fieldsState.phone,
            onValueChange = viewModel::onPhoneChanged,
            label = strings.mobileNumber,
            leadingIcon = Icons.Default.Phone,
            errorText = fieldsState.phoneError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Email Address
        AppTextField(
            value = fieldsState.email,
            onValueChange = viewModel::onEmailChanged,
            label = strings.emailAddress,
            leadingIcon = Icons.Default.Email,
            errorText = fieldsState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Password
        AppTextField(
            value = fieldsState.password,
            onValueChange = viewModel::onPasswordChanged,
            label = "${strings.password} (min. 6)",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorText = fieldsState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        // Confirm Password
        AppTextField(
            value = fieldsState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChanged,
            label = strings.confirmPassword,
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorText = fieldsState.confirmPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        // PROFESSIONAL SPECIFIC FIELDS (Surveyor, Legal Advisor, Document Writer)
        if (fieldsState.selectedRole != UserRole.USER) {
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            val cardTitle = when (fieldsState.selectedRole) {
                UserRole.LEGAL_ADVISOR -> strings.legalEnrollmentNumber
                UserRole.DOCUMENT_WRITER -> strings.documentWriterLicenseNumber
                else -> strings.surveyorLicenseInfo
            }

            val licenseLabel = when (fieldsState.selectedRole) {
                UserRole.LEGAL_ADVISOR -> strings.legalEnrollmentNumber
                UserRole.DOCUMENT_WRITER -> strings.documentWriterLicenseNumber
                else -> strings.licenseNumber
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                    Text(
                        text = cardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                    AppTextField(
                        value = fieldsState.licenseNumber,
                        onValueChange = viewModel::onLicenseNumberChanged,
                        label = licenseLabel,
                        leadingIcon = Icons.Default.Badge,
                        errorText = fieldsState.licenseNumberError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                    AppTextField(
                        value = fieldsState.experienceYears,
                        onValueChange = viewModel::onExperienceYearsChanged,
                        label = strings.experienceYears,
                        leadingIcon = Icons.Default.Work,
                        errorText = fieldsState.experienceYearsError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                    Text(
                        text = "${strings.practiceDistricts} (${fieldsState.selectedServiceAreas.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = strings.chooseDistrictsHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Constants.AP_DISTRICTS.forEach { district ->
                            val isSelected = fieldsState.selectedServiceAreas.contains(district)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleServiceArea(district) },
                                label = { Text(district, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    fieldsState.serviceAreaError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingXLarge))

        PrimaryButton(
            text = strings.registerButton,
            onClick = {
                viewModel.register { email ->
                    onNavigateToVerify(email)
                }
            },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.alreadyHaveAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = strings.loginLink,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))
    }
}
