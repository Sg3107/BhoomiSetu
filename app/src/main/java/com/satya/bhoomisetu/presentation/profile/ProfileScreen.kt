package com.satya.bhoomisetu.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.domain.model.VerificationStatus
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.localization.LanguageManager
import com.satya.bhoomisetu.util.localization.LanguageSelectionDialog
import com.satya.bhoomisetu.util.localization.LanguageSwitchAction
import com.satya.bhoomisetu.util.localization.LocalAppLanguage
import com.satya.bhoomisetu.util.localization.LocalAppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    languageManager: LanguageManager,
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val surveyorProfile by viewModel.surveyorProfile.collectAsState()
    val strings = LocalAppStrings.current
    val currentLang = LocalAppLanguage.current
    val scope = rememberCoroutineScope()
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLang,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { selected ->
                scope.launch {
                    languageManager.setLanguage(selected)
                    showLanguageDialog = false
                }
            }
        )
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (user.role == UserRole.SURVEYOR) {
                viewModel.fetchSurveyorProfile(user.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.myProfile, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    LanguageSwitchAction(
                        languageManager = languageManager,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.paddingMedium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val roleText = when (currentUser?.role) {
                    UserRole.SURVEYOR -> strings.roleSurveyor
                    UserRole.LEGAL_ADVISOR -> strings.roleLegalAdvisor
                    UserRole.DOCUMENT_WRITER -> strings.roleDocumentWriter
                    else -> strings.roleCitizen
                }

                // Header User Identity Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = MaterialTheme.shapes.extraLarge
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                        Text(
                            text = currentUser?.name ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingXSmall))

                        AssistChip(
                            onClick = { },
                            label = { Text(roleText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                // Contact & Account Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                        Text(
                            text = strings.contactInfo,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = strings.mobileNumber,
                            value = currentUser?.phone ?: "N/A"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))

                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = strings.emailAddress,
                            value = currentUser?.email ?: "N/A"
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))

                        ProfileInfoRow(
                            icon = Icons.Default.Badge,
                            label = strings.userRole,
                            value = roleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                // Language Preference Setting Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = strings.languageSettings,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.languageSettings,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${strings.currentLanguageLabel}: ${currentLang.localizedName} (${currentLang.displayName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (currentLang == com.satya.bhoomisetu.util.localization.AppLanguage.ENGLISH) "మార్చండి" else "Change",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Surveyor Specific License Information Card
                if (currentUser?.role == UserRole.SURVEYOR) {
                    Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                            Text(
                                text = strings.surveyorLicenseInfo,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                            ProfileInfoRow(
                                icon = Icons.Default.Badge,
                                label = strings.licenseNumber,
                                value = surveyorProfile?.licenseNumber ?: "N/A"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))

                            ProfileInfoRow(
                                icon = Icons.Default.Work,
                                label = strings.experienceYears,
                                value = "${surveyorProfile?.yearsOfExperience ?: 0} Years"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))

                            ProfileInfoRow(
                                icon = Icons.Default.CheckCircle,
                                label = strings.verificationStatus,
                                value = surveyorProfile?.verificationStatus?.name ?: VerificationStatus.PENDING.name
                            )

                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                            Text(
                                text = "${strings.practiceDistricts}:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(Dimens.paddingXSmall))

                            FlowRow(modifier = Modifier.fillMaxWidth()) {
                                surveyorProfile?.serviceAreas?.forEach { area ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(area) },
                                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingXLarge))

                // Logout Button
                Button(
                    onClick = { viewModel.logout(onLoggedOut) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = strings.logoutButton
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Text(
                        text = strings.logoutButton,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(Dimens.paddingMedium))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
