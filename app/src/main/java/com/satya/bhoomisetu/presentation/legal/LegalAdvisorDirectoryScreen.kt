package com.satya.bhoomisetu.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.LegalAdvisorRequest
import com.satya.bhoomisetu.domain.model.LegalAdvisorSummary
import com.satya.bhoomisetu.presentation.components.AppTextField
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.ApLocationData
import com.satya.bhoomisetu.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalAdvisorDirectoryScreen(
    viewModel: LegalAdvisorDirectoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRequestDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAdvisorForRequest by remember { mutableStateOf<LegalAdvisorSummary?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Legal Advisors & Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAdvisors() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Available Advisors (${uiState.advisors.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("My Requests (${uiState.myRequests.size})") }
                )
            }

            if (uiState.selectedTab == 0) {
                // Advisors Tab with District Filtering
                Column(modifier = Modifier.fillMaxSize()) {
                    // District selector header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.paddingXSmall))
                        Text(
                            text = "Filter by AP District:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Horizontal District Chips (All AP Districts)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Dimens.paddingMedium, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedDistrict == "All Districts",
                            onClick = { viewModel.onDistrictSelected("All Districts") },
                            label = { Text("All Districts") }
                        )
                        Constants.AP_DISTRICTS.forEach { district ->
                            FilterChip(
                                selected = uiState.selectedDistrict == district,
                                onClick = { viewModel.onDistrictSelected(district) },
                                label = { Text(district) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.advisors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(Dimens.paddingLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )


                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                Text(
                                    text = "No legal advisors registered in ${uiState.selectedDistrict} yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = Dimens.paddingMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                        ) {
                            items(uiState.advisors) { advisor ->
                                LegalAdvisorCard(
                                    advisor = advisor,
                                    onRequestClick = { selectedAdvisorForRequest = advisor }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(Dimens.paddingLarge)) }
                        }
                    }
                }
            } else {
                // My Requests Tab
                if (uiState.myRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.paddingLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                            Text(
                                text = "You haven't submitted any legal verification requests yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                    ) {
                        items(uiState.myRequests) { request ->
                            UserLegalRequestCard(
                                request = request,
                                onClick = { onNavigateToRequestDetail(request.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating a new verification request to the selected advisor
    selectedAdvisorForRequest?.let { advisor ->
        CreateRequestDialog(
            advisor = advisor,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { selectedAdvisorForRequest = null },
            onSubmit = { propertyTitle, district, mandal, village, surveyNumber, description ->
                viewModel.createVerificationRequest(
                    advisorId = advisor.id,
                    district = district,
                    mandal = mandal,
                    village = village,
                    surveyNumber = surveyNumber,
                    propertyTitle = propertyTitle,
                    description = description,
                    onSuccess = { newRequestId ->
                        selectedAdvisorForRequest = null
                        onNavigateToRequestDetail(newRequestId)
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegalAdvisorCard(
    advisor: LegalAdvisorSummary,
    onRequestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Advisor",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )


                }

                Spacer(modifier = Modifier.width(Dimens.paddingMedium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = advisor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Enrollment",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Enrollment: ${advisor.enrollmentNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            // Privacy Guarantee Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Private",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Phone number & direct messaging unlocked upon acceptance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Text(
                text = "Practice Districts:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                advisor.districts.forEach { dist ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = dist,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Button(
                onClick = onRequestClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(imageVector = Icons.Default.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                Text("Send Request for Land Verification")
            }
        }
    }
}

@Composable
fun UserLegalRequestCard(
    request: LegalAdvisorRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.propertyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                val (statusColor, statusBg) = when (request.status) {
                    "ACCEPTED" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
                    "VERIFIED" -> Pair(Color(0xFF1565C0), Color(0xFFE3F2FD))
                    "NEEDS_CHANGES" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0))
                    "REJECTED" -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE))
                    else -> Pair(Color(0xFFF57C00), Color(0xFFFFF8E1))
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingXSmall))

            Text(
                text = "Assigned Advisor: ${request.advisorName} (${request.advisorEnrollment})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.district}${if (request.mandal.isNotBlank()) ", ${request.mandal}" else ""}${if (request.surveyNumber.isNotBlank()) " | Sy: ${request.surveyNumber}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            if (request.status == "PENDING") {
                Text(
                    text = "🔒 Phone number and in-app chat will unlock once accepted by advisor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Text(
                    text = "✓ Contact unlocked & ${request.documents.size} documents uploaded",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestDialog(
    advisor: LegalAdvisorSummary,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (propertyTitle: String, district: String, mandal: String, village: String, surveyNumber: String, description: String) -> Unit
) {
    var propertyTitle by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf(advisor.districts.firstOrNull() ?: Constants.AP_DISTRICTS.first()) }
    var districtMenuExpanded by remember { mutableStateOf(false) }
    var mandal by remember { mutableStateOf("") }
    var mandalExpanded by remember { mutableStateOf(false) }
    var village by remember { mutableStateOf("") }
    var villageExpanded by remember { mutableStateOf(false) }
    var surveyNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }

    val availableDistricts = remember(advisor.districts) {
        if (advisor.districts.isNotEmpty()) advisor.districts else Constants.AP_DISTRICTS
    }

    val mandalOptions = remember(selectedDistrict) {
        ApLocationData.getMandals(selectedDistrict)
    }
    val filteredMandals = remember(mandal, mandalOptions) {
        if (mandal.isBlank()) mandalOptions
        else {
            val matching = mandalOptions.filter { it.contains(mandal.trim(), ignoreCase = true) }
            if (matching.isEmpty()) mandalOptions else matching
        }
    }

    val villageOptions = remember(mandal) {
        if (mandal.isNotBlank()) ApLocationData.getVillages(mandal) else emptyList()
    }
    val filteredVillages = remember(village, villageOptions) {
        if (village.isBlank()) villageOptions
        else {
            val matching = villageOptions.filter { it.contains(village.trim(), ignoreCase = true) }
            if (matching.isEmpty()) villageOptions else matching
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Request Legal Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "To: ${advisor.name} (${advisor.enrollmentNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    value = propertyTitle,
                    onValueChange = { propertyTitle = it; titleError = null },
                    label = "Property Title / Land Summary (e.g. 2.5 Acres Agri Land)",
                    errorText = titleError
                )

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                // District Dropdown
                Text("District *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = districtMenuExpanded,
                    onExpandedChange = { districtMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDistrict,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = districtMenuExpanded,
                        onDismissRequest = { districtMenuExpanded = false }
                    ) {
                        availableDistricts.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist) },
                                onClick = {
                                    selectedDistrict = dist
                                    mandal = ""
                                    village = ""
                                    districtMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                // Mandal Dropdown (Searchable / Selectable)
                Text("Mandal *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = mandalExpanded,
                    onExpandedChange = { mandalExpanded = it }
                ) {
                    OutlinedTextField(
                        value = mandal,
                        onValueChange = {
                            mandal = it
                            mandalExpanded = true
                        },
                        placeholder = { Text("Select or type Mandal") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mandalExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    if (filteredMandals.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = mandalExpanded,
                            onDismissRequest = { mandalExpanded = false },
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            filteredMandals.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        mandal = m
                                        mandalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                // Village Dropdown (Searchable / Selectable)
                Text("Village / Locality", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = villageExpanded,
                    onExpandedChange = { villageExpanded = it }
                ) {
                    OutlinedTextField(
                        value = village,
                        onValueChange = {
                            village = it
                            villageExpanded = true
                        },
                        placeholder = { Text("Select or type Village") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    if (filteredVillages.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = villageExpanded,
                            onDismissRequest = { villageExpanded = false },
                            modifier = Modifier.heightIn(max = 260.dp)
                        ) {
                            filteredVillages.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = {
                                        village = v
                                        villageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                AppTextField(
                    value = surveyNumber,
                    onValueChange = { surveyNumber = it },
                    label = "Survey / Sub-Division Number"
                )

                Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descError = null },
                    label = { Text("Verification Instructions / Questions *") },
                    placeholder = { Text("Specify what documents you want the Legal Advisor to check before purchasing (e.g. Title chain, EC, 1B)...") },
                    isError = descError != null,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                descError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasErr = false
                    if (propertyTitle.trim().isBlank()) {
                        titleError = "Property title is required"
                        hasErr = true
                    }
                    if (description.trim().isBlank()) {
                        descError = "Description is required"
                        hasErr = true
                    }
                    if (!hasErr) {
                        onSubmit(propertyTitle.trim(), selectedDistrict, mandal.trim(), village.trim(), surveyNumber.trim(), description.trim())
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}
