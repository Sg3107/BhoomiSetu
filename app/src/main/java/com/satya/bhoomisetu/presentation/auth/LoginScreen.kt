package com.satya.bhoomisetu.presentation.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.satya.bhoomisetu.presentation.components.AppTextField
import com.satya.bhoomisetu.presentation.components.ErrorBanner
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Constants
import com.satya.bhoomisetu.util.localization.LanguageManager
import com.satya.bhoomisetu.util.localization.LanguageSwitchAction
import com.satya.bhoomisetu.util.localization.LocalAppStrings

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    languageManager: LanguageManager,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onLoginSuccess: () -> Unit
) {
    val fieldsState by viewModel.fieldsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    viewModel.loginWithGoogle(idToken) {
                        onLoginSuccess()
                    }
                } else {
                    Toast.makeText(context, "Google ID token missing. Please ensure Web Client ID is configured.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In failed (${e.statusCode}): ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        Icon(
            imageVector = Icons.Default.Landscape,
            contentDescription = strings.appName,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        Text(
            text = strings.welcomeBack,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = strings.loginSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        uiState.errorMessage?.let { error ->
            ErrorBanner(message = error)
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        }

        // Email or Mobile Number
        AppTextField(
            value = fieldsState.phoneOrEmail,
            onValueChange = viewModel::onPhoneOrEmailChanged,
            label = strings.emailOrMobile,
            leadingIcon = Icons.Default.Person,
            errorText = fieldsState.phoneOrEmailError,
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
            label = strings.password,
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorText = fieldsState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.login(
                        onSuccess = { onLoginSuccess() },
                        onRequiresVerification = { unverifiedEmail ->
                            onNavigateToVerify(unverifiedEmail)
                        }
                    )
                }
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text(
                    text = strings.forgotPassword,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        PrimaryButton(
            text = strings.loginButton,
            onClick = {
                viewModel.login(
                    onSuccess = { onLoginSuccess() },
                    onRequiresVerification = { unverifiedEmail ->
                        onNavigateToVerify(unverifiedEmail)
                    }
                )
            },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        // Divider with OR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " ${strings.orDivider} ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        // Google Sign-In Button
        OutlinedButton(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = strings.continueWithGoogle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.dontHaveAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = strings.registerLink,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingLarge))
    }
}
