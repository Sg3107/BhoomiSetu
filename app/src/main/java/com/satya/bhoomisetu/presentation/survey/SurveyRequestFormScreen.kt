package com.satya.bhoomisetu.presentation.survey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.presentation.components.ErrorBanner
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.ApLocationData
import com.satya.bhoomisetu.util.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyRequestFormScreen(
    viewModel: SurveyRequestFormViewModel,
    onNavigateBack: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    var district by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var surveyNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var districtExpanded by remember { mutableStateOf(false) }
    var mandalExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }
    var surveyExpanded by remember { mutableStateOf(false) }
    var savedLandsExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val savedProperties by viewModel.savedProperties.collectAsState()

    // Aggregate districts from predefined AP list and any saved properties
    val allDistricts = remember(savedProperties) {
        val list = linkedSetOf<String>()
        list.addAll(ApLocationData.DISTRICTS)
        savedProperties.forEach { prop ->
            if (prop.district.isNotBlank()) list.add(prop.district)
        }
        list.toList()
    }

    // Mandal options based on selected district and saved properties
    val mandalOptions = remember(district, savedProperties) {
        val list = linkedSetOf<String>()
        // Saved properties for this district first
        savedProperties.filter {
            district.isBlank() || it.district.equals(district, ignoreCase = true)
        }.forEach { prop ->
            if (prop.mandal.isNotBlank()) list.add(prop.mandal)
        }
        // Then AP location data mandals
        if (district.isNotBlank()) {
            list.addAll(ApLocationData.getMandals(district))
        }
        list.toList()
    }

    // Filtered mandals matching typed input
    val filteredMandals = remember(mandal, mandalOptions) {
        if (mandal.isBlank()) mandalOptions
        else {
            val matching = mandalOptions.filter { it.contains(mandal.trim(), ignoreCase = true) }
            if (matching.isEmpty()) mandalOptions else matching
        }
    }

    // Village options based on selected mandal/district and saved properties
    val villageOptions = remember(mandal, district, savedProperties) {
        val list = linkedSetOf<String>()
        savedProperties.filter {
            (district.isBlank() || it.district.equals(district, ignoreCase = true)) &&
            (mandal.isBlank() || it.mandal.equals(mandal, ignoreCase = true))
        }.forEach { prop ->
            if (prop.village.isNotBlank()) list.add(prop.village)
        }
        if (mandal.isNotBlank()) {
            list.addAll(ApLocationData.getVillages(mandal))
        }
        list.toList()
    }

    // Filtered villages matching typed input
    val filteredVillages = remember(village, villageOptions) {
        if (village.isBlank()) villageOptions
        else {
            val matching = villageOptions.filter { it.contains(village.trim(), ignoreCase = true) }
            if (matching.isEmpty()) villageOptions else matching
        }
    }

    // Survey numbers from saved properties and common AP parcel numbers
    val surveyOptions = remember(village, mandal, district, savedProperties) {
        val list = linkedSetOf<String>()
        savedProperties.filter {
            (district.isBlank() || it.district.equals(district, ignoreCase = true)) &&
            (mandal.isBlank() || it.mandal.equals(mandal, ignoreCase = true)) &&
            (village.isBlank() || it.village.equals(village, ignoreCase = true))
        }.forEach { prop ->
            if (prop.surveyNumber.isNotBlank()) list.add(prop.surveyNumber)
        }
        // Fallback to all saved property survey numbers if none matched specifically
        if (list.isEmpty()) {
            savedProperties.forEach { prop ->
                if (prop.surveyNumber.isNotBlank()) list.add(prop.surveyNumber)
            }
        }
        list.addAll(ApLocationData.COMMON_SURVEY_NUMBERS)
        list.toList()
    }

    val filteredSurveys = remember(surveyNumber, surveyOptions) {
        if (surveyNumber.isBlank()) surveyOptions
        else {
            val matching = surveyOptions.filter { it.contains(surveyNumber.trim(), ignoreCase = true) }
            if (matching.isEmpty()) surveyOptions else matching
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is Result.Success) {
            viewModel.resetState()
            onRequestSubmitted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Survey") },
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
                .padding(Dimens.paddingMedium)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Fill in the details to request a certified surveyor for your land.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // Quick Picker from User's Saved Lands
            if (savedProperties.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Landscape,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                            Text(
                                text = "Auto-fill from My Saved Lands",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        ExposedDropdownMenuBox(
                            expanded = savedLandsExpanded,
                            onExpandedChange = { savedLandsExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = if (savedProperties.any { it.surveyNumber == surveyNumber && it.district == district }) {
                                    val matched = savedProperties.first { it.surveyNumber == surveyNumber && it.district == district }
                                    "${matched.nickname} (Sy: ${matched.surveyNumber}, ${matched.mandal})"
                                } else "Tap to choose a saved property...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = savedLandsExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            )

                            ExposedDropdownMenu(
                                expanded = savedLandsExpanded,
                                onDismissRequest = { savedLandsExpanded = false },
                                modifier = Modifier.heightIn(max = 260.dp)
                            ) {
                                savedProperties.forEach { prop ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(prop.nickname, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${prop.district}, ${prop.mandal}${if (prop.village.isNotBlank()) ", " + prop.village else ""} | Sy: ${prop.surveyNumber}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            district = prop.district
                                            mandal = prop.mandal
                                            village = prop.village
                                            surveyNumber = prop.surveyNumber
                                            savedLandsExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            }

            if (uiState is Result.Error) {
                ErrorBanner(
                    message = (uiState as Result.Error).message,
                    onDismiss = { viewModel.resetState() }
                )
                Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            }

            // 1. DISTRICT DROPDOWN (Scroll Down)
            Text(
                text = "District *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = districtExpanded,
                onExpandedChange = { if (uiState !is Result.Loading) districtExpanded = it }
            ) {
                OutlinedTextField(
                    value = district,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select District (Scroll down)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = uiState !is Result.Loading
                )

                ExposedDropdownMenu(
                    expanded = districtExpanded,
                    onDismissRequest = { districtExpanded = false },
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    allDistricts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                if (district != dist) {
                                    district = dist
                                    // Reset mandal if not in the new district's list
                                    val newMandals = ApLocationData.getMandals(dist)
                                    if (mandal.isNotBlank() && !newMandals.any { it.equals(mandal, ignoreCase = true) }) {
                                        mandal = ""
                                        village = ""
                                    }
                                }
                                districtExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // 2. MANDAL DROPDOWN (Scroll Down & Editable)
            Text(
                text = "Mandal *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = mandalExpanded,
                onExpandedChange = { if (uiState !is Result.Loading) mandalExpanded = it }
            ) {
                OutlinedTextField(
                    value = mandal,
                    onValueChange = {
                        mandal = it
                        mandalExpanded = true
                    },
                    placeholder = { Text("Select or type Mandal") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationCity, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = mandalExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = uiState !is Result.Loading
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

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // 3. VILLAGE DROPDOWN (Scroll Down & Editable)
            Text(
                text = "Village (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = villageExpanded,
                onExpandedChange = { if (uiState !is Result.Loading) villageExpanded = it }
            ) {
                OutlinedTextField(
                    value = village,
                    onValueChange = {
                        village = it
                        villageExpanded = true
                    },
                    placeholder = { Text("Select or type Village") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = uiState !is Result.Loading
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

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // 4. SURVEY NUMBER DROPDOWN (Scroll Down & Editable)
            Text(
                text = "Survey Number (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = surveyExpanded,
                onExpandedChange = { if (uiState !is Result.Loading) surveyExpanded = it }
            ) {
                OutlinedTextField(
                    value = surveyNumber,
                    onValueChange = {
                        surveyNumber = it
                        surveyExpanded = true
                    },
                    placeholder = { Text("e.g. 124/2 or select from list") },
                    leadingIcon = {
                        Icon(Icons.Default.Pin, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = surveyExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = uiState !is Result.Loading
                )

                if (filteredSurveys.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = surveyExpanded,
                        onDismissRequest = { surveyExpanded = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        filteredSurveys.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    surveyNumber = s
                                    surveyExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // 5. DESCRIPTION
            Text(
                text = "Description / Reason *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Describe your land survey requirements (e.g. boundary demarcation, partition, fencing)...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                enabled = uiState !is Result.Loading
            )

            Spacer(modifier = Modifier.height(Dimens.paddingXLarge))

            PrimaryButton(
                text = "Submit Request",
                onClick = {
                    viewModel.submitRequest(
                        district = district.trim(),
                        mandal = mandal.trim(),
                        village = village.trim(),
                        surveyNumber = surveyNumber.trim(),
                        description = description.trim()
                    )
                },
                isLoading = uiState is Result.Loading,
                enabled = district.isNotBlank() && mandal.isNotBlank() && description.isNotBlank()
            )
        }
    }
}
