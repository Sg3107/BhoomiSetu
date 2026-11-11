package com.satya.bhoomisetu.presentation.documentwriter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.DeedTypes
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.DocumentWriterSummary
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.ApLocationData
import com.satya.bhoomisetu.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentWriterDirectoryScreen(
    viewModel: DocumentWriterDirectoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val localContext = LocalContext.current

    var showRequestDialog by remember { mutableStateOf(false) }
    var selectedWriterForHire by remember { mutableStateOf<DocumentWriterSummary?>(null) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(localContext, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            Toast.makeText(localContext, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certified Document Writers", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Find Writers") },
                    icon = { Icon(Icons.Default.HistoryEdu, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("My Deed Requests") },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) }
                )
            }

            if (uiState.selectedTab == 0) {
                // Find Document Writers Tab
                DistrictFilterRow(
                    selectedDistrict = uiState.selectedDistrict,
                    onDistrictSelected = { viewModel.onDistrictSelected(it) }
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.writers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Dimens.paddingLarge)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                            Text(
                                "No certified document writers found in ${uiState.selectedDistrict}.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                    ) {
                        items(uiState.writers) { writer ->
                            DocumentWriterCard(
                                writer = writer,
                                onRequestDrafting = {
                                    selectedWriterForHire = writer
                                    showRequestDialog = true
                                }
                            )
                        }
                    }
                }
            } else {
                // My Requests Tab
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.myRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(Dimens.paddingLarge)
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                            Text(
                                "You haven't submitted any deed drafting requests yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
                    ) {
                        items(uiState.myRequests) { req ->
                            ClientWriterRequestCard(
                                request = req,
                                onClick = { onNavigateToDetail(req.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRequestDialog && selectedWriterForHire != null) {
        CreateDeedRequestDialog(
            writer = selectedWriterForHire!!,
            isSubmitting = uiState.isSubmitting,
            onDismiss = {
                showRequestDialog = false
                selectedWriterForHire = null
            },
            onSubmit = { district, mandal, village, surveyNumber, deedType, propertyTitle, description ->
                viewModel.submitWritingRequest(
                    writerId = selectedWriterForHire!!.id,
                    district = district,
                    mandal = mandal,
                    village = village,
                    surveyNumber = surveyNumber,
                    deedType = deedType,
                    propertyTitle = propertyTitle,
                    description = description,
                    onSuccess = { createdId ->
                        showRequestDialog = false
                        selectedWriterForHire = null
                        onNavigateToDetail(createdId)
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistrictFilterRow(
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val districts = listOf("All Districts") + Constants.AP_DISTRICTS

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
        Text(
            text = "District:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(Dimens.paddingSmall))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = selectedDistrict,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                districts.forEach { dist ->
                    DropdownMenuItem(
                        text = { Text(dist) },
                        onClick = {
                            onDistrictSelected(dist)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentWriterCard(
    writer: DocumentWriterSummary,
    onRequestDrafting: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = writer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Lic: ${writer.licenseNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onRequestDrafting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Draft")
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Work,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${writer.yearsOfExperience} years drafting experience in AP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (writer.districts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Text(
                    text = "Authorized Districts:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    writer.districts.forEach { dist ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(dist, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientWriterRequestCard(
    request: DocumentWriterRequest,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(request.createdAt) { dateFormat.format(Date(request.createdAt)) }

    val statusColor = when (request.status) {
        "COMPLETED" -> Color(0xFF2E7D32)
        "DRAFT_UPLOADED" -> Color(0xFF1565C0)
        "ACCEPTED" -> Color(0xFF00796B)
        "REJECTED" -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }

    val statusText = when (request.status) {
        "COMPLETED" -> "Completed"
        "DRAFT_UPLOADED" -> "Draft Deed Ready"
        "ACCEPTED" -> "Accepted / In Progress"
        "REJECTED" -> "Declined"
        else -> "Pending Writer Review"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
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
                    text = request.deedType.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = request.propertyTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.district}${if (request.mandal.isNotBlank()) ", " + request.mandal else ""}${if (request.surveyNumber.isNotBlank()) " | Sy: " + request.surveyNumber else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Writer: ${request.writerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (request.draftDocuments.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${request.draftDocuments.size} Draft(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDeedRequestDialog(
    writer: DocumentWriterSummary,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (district: String, mandal: String, village: String, surveyNumber: String, deedType: String, propertyTitle: String, description: String) -> Unit
) {
    var selectedDeedType by remember { mutableStateOf("SALE_DEED") }
    var deedMenuExpanded by remember { mutableStateOf(false) }

    var selectedDistrict by remember {
        mutableStateOf(writer.districts.firstOrNull() ?: Constants.AP_DISTRICTS.first())
    }
    var districtMenuExpanded by remember { mutableStateOf(false) }

    var mandal by remember { mutableStateOf("") }
    var mandalExpanded by remember { mutableStateOf(false) }
    var village by remember { mutableStateOf("") }
    var villageExpanded by remember { mutableStateOf(false) }
    var surveyNumber by remember { mutableStateOf("") }
    var propertyTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val availableDistricts = remember(writer.districts) {
        if (writer.districts.isNotEmpty()) writer.districts else Constants.AP_DISTRICTS
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
                Text("Order Deed Drafting", fontWeight = FontWeight.Bold)
                Text(
                    "Writer: ${writer.name} (Lic: ${writer.licenseNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                Text("Deed Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = deedMenuExpanded,
                    onExpandedChange = { deedMenuExpanded = it }
                ) {
                    val currentDeed = DeedTypes.ALL.find { it.code == selectedDeedType }?.title ?: selectedDeedType
                    OutlinedTextField(
                        value = currentDeed,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deedMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = deedMenuExpanded,
                        onDismissRequest = { deedMenuExpanded = false }
                    ) {
                        DeedTypes.ALL.forEach { deed ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(deed.title, fontWeight = FontWeight.Bold)
                                        Text(deed.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedDeedType = deed.code
                                    deedMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("District:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                            .fillMaxWidth()
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

                // Mandal Dropdown (Searchable / Selectable)
                Text("Mandal *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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

                // Village Dropdown (Searchable / Selectable)
                Text("Village / Municipality", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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

                OutlinedTextField(
                    value = surveyNumber,
                    onValueChange = { surveyNumber = it },
                    label = { Text("Survey Number / Plot No") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = propertyTitle,
                    onValueChange = { propertyTitle = it },
                    label = { Text("Property Summary *") },
                    placeholder = { Text("e.g., 200 Sq. Yards Land at Poranki") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Specific Instructions & Party Details *") },
                    placeholder = { Text("Mention Buyer & Seller names, consideration amount, and any specific clauses.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (propertyTitle.isNotBlank() && description.isNotBlank()) {
                        onSubmit(
                            selectedDistrict,
                            mandal,
                            village,
                            surveyNumber,
                            selectedDeedType,
                            propertyTitle,
                            description
                        )
                    }
                },
                enabled = !isSubmitting && propertyTitle.isNotBlank() && description.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Order")
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
