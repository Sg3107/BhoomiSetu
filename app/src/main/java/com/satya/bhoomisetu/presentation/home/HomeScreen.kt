package com.satya.bhoomisetu.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.presentation.components.SectionCard
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.localization.LanguageManager
import com.satya.bhoomisetu.util.localization.LanguageSwitchAction
import com.satya.bhoomisetu.util.localization.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    languageManager: LanguageManager,
    onNavigateToLandSearch: () -> Unit,
    onNavigateToEncumbrance: () -> Unit,
    onNavigateToSurveyMarketplace: () -> Unit,
    onNavigateToMyJobs: () -> Unit,
    onNavigateToLegalAdvisors: () -> Unit,
    onNavigateToLegalAdvisorDashboard: () -> Unit,
    onNavigateToDocumentWriters: () -> Unit,
    onNavigateToDocumentWriterDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSurveyMap: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    LanguageSwitchAction(
                        languageManager = languageManager,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val isSurveyor = currentUser?.role == UserRole.SURVEYOR
                val isLegalAdvisor = currentUser?.role == UserRole.LEGAL_ADVISOR
                val isDocumentWriter = currentUser?.role == UserRole.DOCUMENT_WRITER

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.navHome) },
                    label = { Text(strings.navHome) }
                )

                if (isLegalAdvisor) {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNavigateToLegalAdvisorDashboard()
                        },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = strings.navOrders) },
                        label = { Text(strings.navOrders) }
                    )
                } else if (isDocumentWriter) {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNavigateToDocumentWriterDashboard()
                        },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = strings.navOrders) },
                        label = { Text(strings.navOrders) }
                    )
                } else if (!isSurveyor) {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNavigateToLandSearch()
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = strings.navSearch) },
                        label = { Text(strings.navSearch) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onNavigateToLegalAdvisors()
                        },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = strings.navAdvisors) },
                        label = { Text(strings.navAdvisors) }
                    )
                } else {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNavigateToSurveyMarketplace()
                        },
                        icon = { Icon(Icons.Default.Map, contentDescription = strings.navMarketplace) },
                        label = { Text(strings.navMarketplace) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onNavigateToMyJobs()
                        },
                        icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
                        label = { Text("Jobs") }
                    )
                }

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToProfile()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = strings.navProfile) },
                    label = { Text(strings.navProfile) }
                )
            }
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
                    .verticalScroll(rememberScrollState())
            ) {
                // User Welcome Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.paddingMedium)
                    ) {
                        Text(
                            text = "${strings.homeGreeting} ${currentUser?.name ?: "User"}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Dimens.paddingXSmall))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val roleText = when (currentUser?.role) {
                                UserRole.SURVEYOR -> strings.roleSurveyor
                                UserRole.LEGAL_ADVISOR -> strings.roleLegalAdvisor
                                UserRole.DOCUMENT_WRITER -> strings.roleDocumentWriter
                                else -> strings.roleCitizen
                            }
                            Text(
                                text = roleText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                Text(
                    text = strings.quickServicesTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                // Unified MeeBhoomi & EC Portal Card
                SectionCard(
                    title = strings.landRecordsAndEc,
                    subtitle = strings.landRecordsAndEcDesc,
                    icon = Icons.Default.Public,
                    onClick = onNavigateToLandSearch
                )

                if (currentUser?.role == UserRole.LEGAL_ADVISOR) {
                    SectionCard(
                        title = strings.legalAdvisors,
                        subtitle = strings.legalAdvisorsDesc,
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToLegalAdvisorDashboard
                    )
                } else {
                    SectionCard(
                        title = strings.legalAdvisors,
                        subtitle = strings.legalAdvisorsDesc,
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToLegalAdvisors
                    )
                }

                if (currentUser?.role == UserRole.DOCUMENT_WRITER) {
                    SectionCard(
                        title = strings.documentWriters,
                        subtitle = strings.documentWritersDesc,
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToDocumentWriterDashboard
                    )
                } else {
                    SectionCard(
                        title = strings.documentWriters,
                        subtitle = strings.documentWritersDesc,
                        icon = Icons.Default.Assignment,
                        onClick = onNavigateToDocumentWriters
                    )
                }

                if (currentUser?.role == UserRole.SURVEYOR) {
                    SectionCard(
                        title = strings.surveyMarketplace,
                        subtitle = strings.surveyMarketplaceDesc,
                        icon = Icons.Default.Map,
                        onClick = onNavigateToSurveyMarketplace
                    )

                    SectionCard(
                        title = "My Survey Jobs",
                        subtitle = "Manage active and completed measurement tasks",
                        icon = Icons.Default.Work,
                        onClick = onNavigateToMyJobs
                    )
                } else {
                    SectionCard(
                        title = strings.surveyMarketplace,
                        subtitle = strings.surveyMarketplaceDesc,
                        icon = Icons.Default.Engineering,
                        onClick = onNavigateToSurveyMarketplace
                    )
                }

                // Bhuvan ISRO Satellite Map — visible to ALL roles
                SectionCard(
                    title = strings.bhuvanMap,
                    subtitle = strings.bhuvanMapDesc,
                    icon = Icons.Default.Map,
                    onClick = onNavigateToSurveyMap
                )
            }
        }
    }
}
