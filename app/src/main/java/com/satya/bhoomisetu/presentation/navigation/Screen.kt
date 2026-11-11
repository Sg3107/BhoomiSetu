package com.satya.bhoomisetu.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object EmailVerification : Screen("email_verification?email={email}") {
        fun createRoute(email: String): String = "email_verification?email=${Uri.encode(email)}"
    }
    object Home : Screen("home")
    object Profile : Screen("profile")

    // Phase 2 Routes -- Property Profiles + Chrome Custom Tabs launch to the real
    // Meebhoomi/IGRS-EC sites (see CustomTabsLauncher for why this replaced the
    // earlier embedded-WebView approach: a real, repeated 403 confirmed the
    // server actively blocks WebView traffic).
    object PropertyList : Screen("property_list")
    object AddProperty : Screen("add_property")
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: String): String = "property_detail/${Uri.encode(propertyId)}"
    }

    // Survey Marketplace Routes
    object SurveyMarketplace : Screen("survey_marketplace") // Surveyor open requests / User create request
    object MySurveyJobs : Screen("my_survey_jobs") // Surveyor jobs
    object MyRequests : Screen("my_requests") // User requests
    object CreateSurveyRequest : Screen("create_survey_request")
    
    object RequestDetail : Screen("request_detail/{requestId}") {
        fun createRoute(requestId: String): String = "request_detail/${Uri.encode(requestId)}"
    }

    // Legal Advisor & Document Verification Routes
    object LegalAdvisorDirectory : Screen("legal_advisor_directory")
    object LegalAdvisorDashboard : Screen("legal_advisor_dashboard")
    object LegalRequestDetail : Screen("legal_request_detail/{requestId}") {
        fun createRoute(requestId: String): String = "legal_request_detail/${Uri.encode(requestId)}"
    }

    // Document Writer Routes
    object DocumentWriterDirectory : Screen("document_writer_directory")
    object DocumentWriterDashboard : Screen("document_writer_dashboard")
    object DocumentWriterDetail : Screen("document_writer_detail/{requestId}") {
        fun createRoute(requestId: String): String = "document_writer_detail/${Uri.encode(requestId)}"
    }

    // Bhuvan Satellite Map — Survey Number Finder via WMS GetFeatureInfo
    object BhuvanSurveyMap : Screen("bhuvan_survey_map")

    object GenericComingSoon : Screen("coming_soon/{featureName}") {
        fun createRoute(featureName: String): String {
            val encodedName = Uri.encode(featureName)
            return "coming_soon/$encodedName"
        }
    }
}

