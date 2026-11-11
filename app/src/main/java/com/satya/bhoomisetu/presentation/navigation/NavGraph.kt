package com.satya.bhoomisetu.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.satya.bhoomisetu.presentation.auth.LoginScreen
import com.satya.bhoomisetu.presentation.auth.LoginViewModel
import com.satya.bhoomisetu.presentation.auth.RegisterScreen
import com.satya.bhoomisetu.presentation.auth.RegisterViewModel
import com.satya.bhoomisetu.presentation.auth.SessionViewModel
import com.satya.bhoomisetu.presentation.home.ComingSoonScreen
import com.satya.bhoomisetu.presentation.home.HomeScreen
import com.satya.bhoomisetu.presentation.home.HomeViewModel
import com.satya.bhoomisetu.presentation.property.AddPropertyScreen
import com.satya.bhoomisetu.presentation.property.PropertyDetailScreen
import com.satya.bhoomisetu.presentation.property.PropertyListScreen
import com.satya.bhoomisetu.presentation.onboarding.OnboardingScreen
import com.satya.bhoomisetu.presentation.profile.ProfileScreen
import com.satya.bhoomisetu.presentation.profile.ProfileViewModel
import com.satya.bhoomisetu.presentation.splash.SplashScreen

@Composable
fun AppNavGraph(
    languageManager: com.satya.bhoomisetu.util.localization.LanguageManager,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionViewModel = sessionViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                languageManager = languageManager,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.GenericComingSoon.createRoute("Password Reset"))
                },
                onNavigateToVerify = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register
        composable(Screen.Register.route) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = registerViewModel,
                languageManager = languageManager,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToVerify = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }

        // Email OTP Verification
        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val emailVerificationViewModel: com.satya.bhoomisetu.presentation.auth.EmailVerificationViewModel = hiltViewModel()
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            com.satya.bhoomisetu.presentation.auth.EmailVerificationScreen(
                email = email,
                viewModel = emailVerificationViewModel,
                languageManager = languageManager,
                onVerificationSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home Dashboard
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                languageManager = languageManager,
                onNavigateToLandSearch = {
                    navController.navigate(Screen.PropertyList.route)
                },
                onNavigateToEncumbrance = {
                    // A Property Profile is now the single entry point to both
                    // Meebhoomi and IGRS-EC (see PropertyDetailScreen).
                    navController.navigate(Screen.PropertyList.route)
                },
                onNavigateToSurveyMarketplace = {
                    val user = homeViewModel.currentUser.value
                    if (user?.role == com.satya.bhoomisetu.domain.model.UserRole.SURVEYOR) {
                        navController.navigate(Screen.SurveyMarketplace.route)
                    } else {
                        navController.navigate(Screen.MyRequests.route)
                    }
                },
                onNavigateToMyJobs = {
                    navController.navigate(Screen.MySurveyJobs.route)
                },
                onNavigateToLegalAdvisors = {
                    navController.navigate(Screen.LegalAdvisorDirectory.route)
                },
                onNavigateToLegalAdvisorDashboard = {
                    navController.navigate(Screen.LegalAdvisorDashboard.route)
                },
                onNavigateToDocumentWriters = {
                    navController.navigate(Screen.DocumentWriterDirectory.route)
                },
                onNavigateToDocumentWriterDashboard = {
                    navController.navigate(Screen.DocumentWriterDashboard.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSurveyMap = {
                    navController.navigate(Screen.BhuvanSurveyMap.route)
                }
            )
        }


        // Profile
        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                languageManager = languageManager,
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // PHASE 2 REAL DESTINATIONS -- Property Profiles + Chrome Custom Tabs
        // 1. Property list (entry point to both Meebhoomi and IGRS-EC per property)
        composable(Screen.PropertyList.route) {
            PropertyListScreen(
                onAddProperty = { navController.navigate(Screen.AddProperty.route) },
                onPropertyClick = { propertyId ->
                    navController.navigate(Screen.PropertyDetail.createRoute(propertyId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 2. Add a property
        composable(Screen.AddProperty.route) {
            AddPropertyScreen(
                onPropertySaved = { propertyId ->
                    navController.navigate(Screen.PropertyDetail.createRoute(propertyId)) {
                        popUpTo(Screen.PropertyList.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Property detail -- launches Custom Tabs to Meebhoomi/IGRS-EC, shows notes timeline
        composable(
            route = Screen.PropertyDetail.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) {
            PropertyDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Survey Marketplace Routes
        composable(Screen.SurveyMarketplace.route) {
            val viewModel: com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceScreen(
                viewModel = viewModel,
                mode = com.satya.bhoomisetu.presentation.survey.MarketplaceMode.SURVEYOR_OPEN_REQUESTS,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateRequest = { navController.navigate(Screen.CreateSurveyRequest.route) },
                onNavigateToRequestDetail = { id -> navController.navigate(Screen.RequestDetail.createRoute(id)) }
            )
        }

        composable(Screen.MySurveyJobs.route) {
            val viewModel: com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceScreen(
                viewModel = viewModel,
                mode = com.satya.bhoomisetu.presentation.survey.MarketplaceMode.SURVEYOR_MY_JOBS,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateRequest = { navController.navigate(Screen.CreateSurveyRequest.route) },
                onNavigateToRequestDetail = { id -> navController.navigate(Screen.RequestDetail.createRoute(id)) }
            )
        }

        composable(Screen.MyRequests.route) {
            val viewModel: com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.survey.SurveyMarketplaceScreen(
                viewModel = viewModel,
                mode = com.satya.bhoomisetu.presentation.survey.MarketplaceMode.USER_MY_REQUESTS,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateRequest = { navController.navigate(Screen.CreateSurveyRequest.route) },
                onNavigateToRequestDetail = { id -> navController.navigate(Screen.RequestDetail.createRoute(id)) }
            )
        }

        composable(Screen.CreateSurveyRequest.route) {
            val viewModel: com.satya.bhoomisetu.presentation.survey.SurveyRequestFormViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.survey.SurveyRequestFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRequestSubmitted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RequestDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) {
            val viewModel: com.satya.bhoomisetu.presentation.survey.RequestDetailViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.survey.RequestDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Legal Advisor Routes
        composable(Screen.LegalAdvisorDirectory.route) {
            val viewModel: com.satya.bhoomisetu.presentation.legal.LegalAdvisorDirectoryViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.legal.LegalAdvisorDirectoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRequestDetail = { requestId ->
                    navController.navigate(Screen.LegalRequestDetail.createRoute(requestId))
                }
            )
        }

        composable(Screen.LegalAdvisorDashboard.route) {
            val viewModel: com.satya.bhoomisetu.presentation.legal.LegalAdvisorDashboardViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.legal.LegalAdvisorDashboardScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRequestDetail = { requestId ->
                    navController.navigate(Screen.LegalRequestDetail.createRoute(requestId))
                }
            )
        }

        composable(
            route = Screen.LegalRequestDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) {
            val viewModel: com.satya.bhoomisetu.presentation.legal.LegalRequestDetailViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.legal.LegalRequestDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Document Writer Routes
        composable(Screen.DocumentWriterDirectory.route) {
            val viewModel: com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterDirectoryViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterDirectoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { requestId ->
                    navController.navigate(Screen.DocumentWriterDetail.createRoute(requestId))
                }
            )
        }

        composable(Screen.DocumentWriterDashboard.route) {
            val viewModel: com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterDashboardViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterDashboardScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { requestId ->
                    navController.navigate(Screen.DocumentWriterDetail.createRoute(requestId))
                }
            )
        }

        composable(
            route = Screen.DocumentWriterDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) {
            val viewModel: com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterRequestDetailViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.documentwriter.DocumentWriterRequestDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GenericComingSoon.route,
            arguments = listOf(navArgument("featureName") { type = NavType.StringType })
        ) { backStackEntry ->
            val featureName = backStackEntry.arguments?.getString("featureName") ?: "Feature"
            ComingSoonScreen(featureName = featureName)
        }

        // Bhuvan Survey Finder Map
        composable(Screen.BhuvanSurveyMap.route) {
            val viewModel: com.satya.bhoomisetu.presentation.map.BhuvanMapViewModel = hiltViewModel()
            com.satya.bhoomisetu.presentation.map.BhuvanSurveyMapScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

