package com.example.mywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mywidget.core.WidgetPackageManager
import com.example.mywidget.core.ProcessResult
import com.example.mywidget.core.UpdateResult
import com.example.mywidget.engine.factory.WidgetFactory
import com.example.mywidget.engine.factory.GridSize
import com.example.mywidget.widget.*
import com.example.mywidget.ui.theme.MyWidgetTheme
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    private lateinit var packageManager: WidgetPackageManager
    private val widgetFactory = WidgetFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageManager = WidgetPackageManager.getInstance(this)

        enableEdgeToEdge()
        setContent {
            MyWidgetTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetPackageScreen()
                }
            }
        }
    }

    @Composable
    fun WidgetPackageScreen() {
        var selectedGridSize by remember { mutableStateOf(GridSize(2, 2)) }
        var jsonText by remember { mutableStateOf("") }
        var processState by remember { mutableStateOf<ProcessState>(ProcessState.Idle) }

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val filePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                processState = ProcessState.Processing
                coroutineScope.launch {
                    when (val result = packageManager.processPackage(uri)) {
                        is ProcessResult.Success -> {
                            processState =
                                ProcessState.Success("Package loaded successfully! JSON extracted.")
                            jsonText = result.extractedPackage.uiJson
                        }

                        is ProcessResult.ValidationError -> {
                            processState =
                                ProcessState.Error("Validation failed: ${result.errors.joinToString()}")
                        }

                        is ProcessResult.Error -> {
                            processState = ProcessState.Error(result.message)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Section
            UploadSection(
                processState = processState,
                onUploadClick = {
                    filePickerLauncher.launch(
                        arrayOf("application/zip", "application/x-zip-compressed")
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Size Selection
            GridSizeSelector(
                selectedGridSize = selectedGridSize,
                onGridSizeSelected = { selectedGridSize = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // JSON Editor
            JsonEditor(
                jsonText = jsonText,
                onJsonChanged = { jsonText = it },
                onApplyChanges = {
                    coroutineScope.launch {
                        when (val result = packageManager.updateUIConfiguration(jsonText)) {
                            is UpdateResult.Success -> {
                                Toast.makeText(
                                    context,
                                    "UI updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is UpdateResult.ValidationError -> {
                                Toast.makeText(
                                    context,
                                    "Validation failed: ${result.errors.joinToString()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            is UpdateResult.Error -> {
                                Toast.makeText(
                                    context,
                                    "Error: ${result.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Widget Actions
            WidgetActions(
                selectedGridSize = selectedGridSize,
                onAddWidget = { gridSize ->
                    requestWidgetPin(gridSize.height, gridSize.width)
                },
                onClearData = {
                    coroutineScope.launch {
                        packageManager.clearAll()
                        jsonText = ""
                        processState = ProcessState.Idle
                        Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    @Composable
    fun UploadSection(
        processState: ProcessState,
        onUploadClick: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onUploadClick,
                    enabled = processState !is ProcessState.Processing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Widget Package (.zip)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (processState) {
                    is ProcessState.Processing -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing package…")
                        }
                    }

                    is ProcessState.Success -> {
                        StatusRow(
                            icon = Icons.Default.Check,
                            text = processState.message,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    is ProcessState.Error -> {
                        StatusRow(
                            icon = Icons.Default.Warning,
                            text = processState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is ProcessState.Idle -> {
                        Text(
                            text = "Select a zip file containing ui.json and resources",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun StatusRow(icon: ImageVector, text: String, color: Color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = color, style = MaterialTheme.typography.bodySmall)
        }
    }

    @Composable
    fun GridSizeSelector(
        selectedGridSize: GridSize,
        onGridSizeSelected: (GridSize) -> Unit
    ) {
        val gridOptions = widgetFactory.getSupportedGridSizes()
        var expanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Widget Grid Size",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedGridSize.toString())
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        gridOptions.forEach { gridSize ->
                            DropdownMenuItem(
                                text = { Text(gridSize.toString()) },
                                onClick = {
                                    onGridSizeSelected(gridSize)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun JsonEditor(
        jsonText: String,
        onJsonChanged: (String) -> Unit,
        onApplyChanges: () -> Unit
    ) {
        var isValid by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf("") }
        var showApplyConfirmation by remember { mutableStateOf(false) }
        val hasContent = jsonText.trim().isNotEmpty()

        LaunchedEffect(jsonText) {
            val trimmed = jsonText.trim()
            isValid = (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]")) ||
                    trimmed.isEmpty()
            errorMessage = if (!isValid) "Invalid JSON format" else ""
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Widget UI JSON Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (hasContent) {
                        StatusRow(
                            icon = Icons.Default.Check,
                            text = "JSON Loaded",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { onJsonChanged(it) },
                    label = { Text("Widget UI JSON") },
                    placeholder = { Text("Upload or paste JSON here…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    isError = !isValid
                )

                if (!isValid) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apply changes to update widget configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { showApplyConfirmation = true },
                        enabled = isValid && hasContent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text("Apply Changes")
                    }
                }
            }
        }

        if (showApplyConfirmation) {
            AlertDialog(
                onDismissRequest = { showApplyConfirmation = false },
                title = { Text("Apply JSON Changes") },
                text = { Text("This will update your widget configuration for all widgets. Continue?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showApplyConfirmation = false
                            onApplyChanges()
                        }
                    ) { Text("Apply") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showApplyConfirmation = false }
                    ) { Text("Cancel") }
                }
            )
        }
    }

    @Composable
    fun WidgetActions(
        selectedGridSize: GridSize,
        onAddWidget: (GridSize) -> Unit,
        onClearData: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onAddWidget(selectedGridSize) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Widget (${selectedGridSize.width}x${selectedGridSize.height})")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClearData,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Clear Data")
            }
        }
    }

    private fun requestWidgetPin(height: Int, width: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val providerClass = when {
            height == 1 && width == 1 -> MyWidgetReceiver1x1::class.java
            height == 1 && width == 2 -> MyWidgetReceiver1x2::class.java
            height == 1 && width == 3 -> MyWidgetReceiver1x3::class.java
            height == 1 && width == 4 -> MyWidgetReceiver1x4::class.java
            height == 2 && width == 1 -> MyWidgetReceiver2x1::class.java
            height == 2 && width == 2 -> MyWidgetReceiver2x2::class.java
            height == 2 && width == 3 -> MyWidgetReceiver2x3::class.java
            height == 2 && width == 4 -> MyWidgetReceiver2x4::class.java
            height == 3 && width == 1 -> MyWidgetReceiver3x1::class.java
            height == 3 && width == 2 -> MyWidgetReceiver3x2::class.java
            height == 3 && width == 3 -> MyWidgetReceiver3x3::class.java
            height == 3 && width == 4 -> MyWidgetReceiver3x4::class.java
            height == 4 && width == 1 -> MyWidgetReceiver4x1::class.java
            height == 4 && width == 2 -> MyWidgetReceiver4x2::class.java
            height == 4 && width == 3 -> MyWidgetReceiver4x3::class.java
            height == 4 && width == 4 -> MyWidgetReceiver4x4::class.java
            else -> MyWidgetReceiver2x2::class.java
        }
        val provider = ComponentName(this, providerClass)
        appWidgetManager.requestPinAppWidget(provider, null, null)
    }
}

/** State for package processing */
sealed class ProcessState {
    object Idle : ProcessState()
    object Processing : ProcessState()
    data class Success(val message: String) : ProcessState()
    data class Error(val message: String) : ProcessState()
}