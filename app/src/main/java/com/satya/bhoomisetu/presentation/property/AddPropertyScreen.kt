package com.satya.bhoomisetu.presentation.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satya.bhoomisetu.domain.model.RecordType
import com.satya.bhoomisetu.presentation.components.AppTextField
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.ApLocationData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    onPropertySaved: (propertyId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddPropertyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var districtExpanded by remember { mutableStateOf(false) }
    var mandalExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    val mandalOptions = remember(state.district) {
        if (state.district.isNotBlank()) ApLocationData.getMandals(state.district) else emptyList()
    }
    val filteredMandals = remember(state.mandal, mandalOptions) {
        if (state.mandal.isBlank()) mandalOptions
        else {
            val matching = mandalOptions.filter { it.contains(state.mandal.trim(), ignoreCase = true) }
            if (matching.isEmpty()) mandalOptions else matching
        }
    }

    val villageOptions = remember(state.mandal) {
        if (state.mandal.isNotBlank()) ApLocationData.getVillages(state.mandal) else emptyList()
    }
    val filteredVillages = remember(state.village, villageOptions) {
        if (state.village.isBlank()) villageOptions
        else {
            val matching = villageOptions.filter { it.contains(state.village.trim(), ignoreCase = true) }
            if (matching.isEmpty()) villageOptions else matching
        }
    }

    LaunchedEffect(state.savedPropertyId) {
        state.savedPropertyId?.let(onPropertySaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a property", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
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
                    .padding(Dimens.paddingLarge)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "This just organizes your own record-keeping -- it doesn't fetch anything from a government site.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Dimens.paddingLarge))

                AppTextField(
                    value = state.nickname,
                    onValueChange = viewModel::onNicknameChange,
                    label = "Nickname (e.g. \"Grandpa's farm\")",
                    leadingIcon = Icons.Default.Badge,
                    errorText = state.fieldErrors["nickname"]
                )
                Spacer(Modifier.height(Dimens.paddingMedium))

                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = !districtExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.district.ifEmpty { "Select district" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("District") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }) {
                        viewModel.districts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district) },
                                onClick = {
                                    viewModel.onDistrictChange(district)
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }
                if (state.fieldErrors["district"] != null) {
                    Text(
                        state.fieldErrors["district"]!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = Dimens.paddingSmall, top = Dimens.paddingXSmall)
                    )
                }
                Spacer(Modifier.height(Dimens.paddingMedium))

                // Mandal Dropdown (Searchable / Selectable)
                ExposedDropdownMenuBox(
                    expanded = mandalExpanded,
                    onExpandedChange = { mandalExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.mandal,
                        onValueChange = {
                            viewModel.onMandalChange(it)
                            mandalExpanded = true
                        },
                        label = { Text("Mandal *") },
                        placeholder = { Text(if (state.district.isBlank()) "Select district first" else "Select or type Mandal") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mandalExpanded) },
                        isError = state.fieldErrors["mandal"] != null,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                        viewModel.onMandalChange(m)
                                        mandalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (state.fieldErrors["mandal"] != null) {
                    Text(
                        state.fieldErrors["mandal"]!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = Dimens.paddingSmall, top = Dimens.paddingXSmall)
                    )
                }
                Spacer(Modifier.height(Dimens.paddingMedium))

                // Village Dropdown (Searchable / Selectable)
                ExposedDropdownMenuBox(
                    expanded = villageExpanded,
                    onExpandedChange = { villageExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.village,
                        onValueChange = {
                            viewModel.onVillageChange(it)
                            villageExpanded = true
                        },
                        label = { Text("Village *") },
                        placeholder = { Text("Select or type Village") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = villageExpanded) },
                        isError = state.fieldErrors["village"] != null,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
                                        viewModel.onVillageChange(v)
                                        villageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (state.fieldErrors["village"] != null) {
                    Text(
                        state.fieldErrors["village"]!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = Dimens.paddingSmall, top = Dimens.paddingXSmall)
                    )
                }
                Spacer(Modifier.height(Dimens.paddingMedium))

                AppTextField(
                    value = state.surveyNumber,
                    onValueChange = viewModel::onSurveyNumberChange,
                    label = "Survey number (e.g. 102/1A)",
                    leadingIcon = Icons.Default.Pin,
                    errorText = state.fieldErrors["surveyNumber"]
                )
                Spacer(Modifier.height(Dimens.paddingMedium))

                Text("Record type", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(Dimens.paddingXSmall))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.recordType == RecordType.ADANGAL,
                        onClick = { viewModel.onRecordTypeChange(RecordType.ADANGAL) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Adangal") }
                    SegmentedButton(
                        selected = state.recordType == RecordType.ROR_1B,
                        onClick = { viewModel.onRecordTypeChange(RecordType.ROR_1B) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("1-B (Ownership)") }
                }

                Spacer(Modifier.height(Dimens.paddingXLarge))
                PrimaryButton(text = "Save property", onClick = viewModel::save, isLoading = state.isSaving)
            }
        }
    }
}
