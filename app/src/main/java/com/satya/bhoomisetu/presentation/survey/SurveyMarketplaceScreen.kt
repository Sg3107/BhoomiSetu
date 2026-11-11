package com.satya.bhoomisetu.presentation.survey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.model.SurveyRequestStatus
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.presentation.components.ErrorBanner
import com.satya.bhoomisetu.presentation.components.LoadingIndicator
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.ApLocationData
import com.satya.bhoomisetu.util.Constants
import com.satya.bhoomisetu.util.Result
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MarketplaceMode {
    USER_MY_REQUESTS,
    SURVEYOR_OPEN_REQUESTS,
    SURVEYOR_MY_JOBS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyMarketplaceScreen(
    viewModel: SurveyMarketplaceViewModel,
    mode: MarketplaceMode,
    onNavigateBack: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToRequestDetail: (String) -> Unit
) {
    val requestsState by viewModel.requests.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedMandal by viewModel.selectedMandal.collectAsState()

    var districtFilterExpanded by remember { mutableStateOf(false) }
    var mandalFilterExpanded by remember { mutableStateOf(false) }

    val mandalFilterOptions = remember(selectedDistrict) {
        if (!selectedDistrict.isNullOrBlank()) ApLocationData.getMandals(selectedDistrict!!) else emptyList()
    }

    LaunchedEffect(mode) {
        when (mode) {
            MarketplaceMode.USER_MY_REQUESTS -> viewModel.loadMyRequests()
            MarketplaceMode.SURVEYOR_OPEN_REQUESTS -> viewModel.loadOpenRequests()
            MarketplaceMode.SURVEYOR_MY_JOBS -> viewModel.loadAssignedRequests()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (mode) {
                            MarketplaceMode.USER_MY_REQUESTS -> "My Requests"
                            MarketplaceMode.SURVEYOR_OPEN_REQUESTS -> "Nearby Requests"
                            MarketplaceMode.SURVEYOR_MY_JOBS -> "My Assigned Jobs"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (userRole != UserRole.SURVEYOR) {
                FloatingActionButton(onClick = onNavigateToCreateRequest) {
                    Icon(Icons.Default.Add, contentDescription = "New Request")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Surveyor Filter Header by District & Mandal
            if (mode == MarketplaceMode.SURVEYOR_OPEN_REQUESTS) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Filter Open Requests:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (selectedDistrict != null || selectedMandal != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { viewModel.filterOpenRequests(null, null) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // District Dropdown Box
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { districtFilterExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = selectedDistrict ?: "All Districts",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selectedDistrict != null) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                                DropdownMenu(
                                    expanded = districtFilterExpanded,
                                    onDismissRequest = { districtFilterExpanded = false },
                                    modifier = Modifier.heightIn(max = 300.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Districts", fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            districtFilterExpanded = false
                                            viewModel.filterOpenRequests(null, null)
                                        }
                                    )
                                    HorizontalDivider()
                                    Constants.AP_DISTRICTS.forEach { dist ->
                                        DropdownMenuItem(
                                            text = { Text(dist) },
                                            onClick = {
                                                districtFilterExpanded = false
                                                viewModel.filterOpenRequests(dist, null)
                                            }
                                        )
                                    }
                                }
                            }

                            // Mandal Dropdown Box
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { if (selectedDistrict != null) mandalFilterExpanded = true },
                                    enabled = selectedDistrict != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = selectedMandal ?: (if (selectedDistrict == null) "Select District" else "All Mandals"),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selectedMandal != null) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                                if (selectedDistrict != null) {
                                    DropdownMenu(
                                        expanded = mandalFilterExpanded,
                                        onDismissRequest = { mandalFilterExpanded = false },
                                        modifier = Modifier.heightIn(max = 300.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All Mandals in $selectedDistrict", fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                mandalFilterExpanded = false
                                                viewModel.filterOpenRequests(selectedDistrict, null)
                                            }
                                        )
                                        HorizontalDivider()
                                        mandalFilterOptions.forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(m) },
                                                onClick = {
                                                    mandalFilterExpanded = false
                                                    viewModel.filterOpenRequests(selectedDistrict, m)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            when (val state = requestsState) {
                is Result.Loading -> {
                    LoadingIndicator()
                }
                is Result.Error -> {
                    ErrorBanner(
                        message = state.message,
                        onDismiss = {
                            when (mode) {
                                MarketplaceMode.USER_MY_REQUESTS -> viewModel.loadMyRequests()
                                MarketplaceMode.SURVEYOR_OPEN_REQUESTS -> viewModel.loadOpenRequests()
                                MarketplaceMode.SURVEYOR_MY_JOBS -> viewModel.loadAssignedRequests()
                            }
                        }
                    )
                }
                is Result.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No requests found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Dimens.paddingMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                        ) {
                            items(state.data) { request ->
                                RequestCard(
                                    request = request,
                                    onClick = { onNavigateToRequestDetail(request.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun RequestCard(
    request: SurveyRequest,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(request.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Text(
                        text = "${request.district}, ${request.mandal}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when(request.status) {
                        SurveyRequestStatus.OPEN -> MaterialTheme.colorScheme.primaryContainer
                        SurveyRequestStatus.ACCEPTED -> Color(0xFFE8F5E9) // Light Green
                        SurveyRequestStatus.COMPLETED -> Color(0xFFE3F2FD) // Light Blue
                        SurveyRequestStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = request.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when(request.status) {
                            SurveyRequestStatus.OPEN -> MaterialTheme.colorScheme.onPrimaryContainer
                            SurveyRequestStatus.ACCEPTED -> Color(0xFF1B5E20)
                            SurveyRequestStatus.COMPLETED -> Color(0xFF0D47A1)
                            SurveyRequestStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By: ${request.requesterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
