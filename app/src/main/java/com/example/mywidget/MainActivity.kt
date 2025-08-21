package com.example.mywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mywidget.widget.MyWidgetReceiver1x1
import com.example.mywidget.widget.MyWidgetReceiver1x2
import com.example.mywidget.widget.MyWidgetReceiver1x3
import com.example.mywidget.widget.MyWidgetReceiver1x4
import com.example.mywidget.widget.MyWidgetReceiver2x1
import com.example.mywidget.widget.MyWidgetReceiver2x2
import com.example.mywidget.widget.MyWidgetReceiver2x3
import com.example.mywidget.widget.MyWidgetReceiver2x4
import com.example.mywidget.widget.MyWidgetReceiver3x1
import com.example.mywidget.widget.MyWidgetReceiver3x2
import com.example.mywidget.widget.MyWidgetReceiver3x3
import com.example.mywidget.widget.MyWidgetReceiver3x4
import com.example.mywidget.widget.MyWidgetReceiver4x1
import com.example.mywidget.widget.MyWidgetReceiver4x2
import com.example.mywidget.widget.MyWidgetReceiver4x3
import com.example.mywidget.widget.MyWidgetReceiver4x4
import com.example.mywidget.datastore.storeUIJson
import com.example.mywidget.fonts.FontRegistry
import com.example.mywidget.json.UiSchemaValidator
import com.example.mywidget.ui.theme.MyWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWidgetTheme {
//                AddWidgetButtons()
                UploadZipScreen {
                    val json = JSONObject(it)
                    val result = UiSchemaValidator.validate(json)
                    if (!result.isValid) {
                        Toast.makeText(this, "Invalid UI: ${result.errors.joinToString()}", Toast.LENGTH_LONG).show()
                    } else {
                        storeUIJson(this@MainActivity, it)
                        requestWidgetPin(3, 3)
                    }
                }
            }
        }
    }

    @Composable
    fun UploadZipScreen(onJsonReady: (String) -> Unit) {
        var jsonContent by remember { mutableStateOf<String?>(null) }
        var status by remember { mutableStateOf("Select a zip file") }

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val filePickerLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                uri?.let {
                    status = "Processing..."
                    coroutineScope.launch {
                        val json = unzipAndStore(context, uri)
                        if (json != null) {
                            jsonContent = json
                            status = "JSON extracted!"
                            onJsonReady(json)
                        } else {
                            status = "Failed to read zip"
                        }
                    }
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { filePickerLauncher.launch(arrayOf("application/zip")) }) {
                Text("Upload Zip File")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(status)

            jsonContent?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it.take(500) + if (it.length > 500) "..." else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    suspend fun unzipAndStore(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            val baseDir = File(context.filesDir, "ui_package").apply {
                // Clean up old extraction
                if (exists()) deleteRecursively()
                mkdirs()
            }
            val fontsDir = File(context.filesDir, "fonts").apply { mkdirs() }

            var jsonString: String? = null

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val name = entry.name

                        // Skip macOS metadata & hidden files
                        if (name.contains("__MACOSX") || name.endsWith(".DS_Store")) {
                            zis.closeEntry()
                            entry = zis.nextEntry
                            continue
                        }

                        val file = when {
                            name.endsWith(".ttf") || name.endsWith(".otf") -> File(fontsDir, name)
                            else -> File(baseDir, name)
                        }

                        if (entry.isDirectory) {
                            if (file.exists() && file.isFile) file.delete()
                            file.mkdirs()
                        } else {
                            val parent = file.parentFile
                            if (parent != null) {
                                if (parent.exists() && parent.isFile) parent.delete()
                                parent.mkdirs()
                            }

                            FileOutputStream(file).use { fos -> zis.copyTo(fos) }

                            // Register font immediately if it is a font file
                            if (file.extension in listOf("ttf", "otf")) {
                                FontRegistry.registerFont(file.nameWithoutExtension, file.absolutePath)
                            }

                            if (file.name.equals("ui.json", ignoreCase = true)) {
                                jsonString = file.readText()
                            }
                        }

                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            return@withContext jsonString
        }

    @Composable
    fun AddWidgetButtons() {
        val gridOptions = (1..4).flatMap { h -> (1..4).map { w -> "${h}x${w}" } }
        var expanded by remember { mutableStateOf(false) }
        var selectedGrid by remember { mutableStateOf(gridOptions[0]) }
        var jsonText by remember { mutableStateOf(loadSampleJson()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp, 100.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GridOptionsDropDown(
                    gridOptions = gridOptions,
                    expanded = expanded,
                    onExpandedChange = { isExpanded ->
                        expanded = isExpanded
                    },
                    selectedGrid = selectedGrid,
                    onGridSelected = { selected ->
                        selectedGrid = selected
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
                JsonToUiConversion(selectedGrid, jsonText) { newJsonText ->
                    jsonText = newJsonText
                }
            }
        }
    }

    @Composable
    fun GridOptionsDropDown(
        gridOptions: List<String>,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        selectedGrid: String,
        onGridSelected: (String) -> Unit
    ) {
        Text("Select Widget Grid Size")

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = selectedGrid,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    label = { Text("Grid Size") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                        )
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .clickable { onExpandedChange(true) },
                    colors = OutlinedTextFieldDefaults.colors().copy(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.width(200.dp)
            ) {
                gridOptions.forEach { grid ->
                    DropdownMenuItem(text = { Text(grid) }, onClick = {
                        onGridSelected(grid)
                        onExpandedChange(false)
                    })
                }
            }
        }
    }

    @Composable
    fun JsonToUiConversion(
        selectedGrid: String,
        jsonText: String,
        onJsonChanged: (String) -> Unit
    ) {
        UiJSONTextField(jsonText, onJsonChanged)
        Spacer(modifier = Modifier.height(16.dp))
        AddJsonUIButton(selectedGrid, jsonText)
    }

    @Composable
    fun UiJSONTextField(jsonText: String, onJSonTextChanged: (String) -> Unit) {
        OutlinedTextField(
            value = jsonText,
            onValueChange = { onJSonTextChanged(it) },
            label = { Text("Widget UI JSON") },
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }

    @Composable
    fun AddJsonUIButton(selectedGrid: String, jsonText: String) {
        Button(
            onClick = {
                val (heightValue, widthValue) = selectedGrid.split("x").map { it.toInt() }
                storeUIJson(this@MainActivity, jsonText)
                requestWidgetPin(heightValue, widthValue)
            }) {
            Text("Add JSON Widget")
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

    private fun loadSampleJson(): String {
        return """
            [
                {"id":"root_ui","type":"column","parentId":null,"attributes":{"margin":"16","background":"#222222","width":"match_parent","height":"match_parent"}},
            
                {"id":"header_row","type":"row","parentId":"root_ui","attributes":{"margin":"8","background":"#444444"}},
                {"id":"body_stack","type":"stack","parentId":"root_ui","attributes":{"margin":"12","background":"#666666"}},
                {"id":"footer_row","type":"row","parentId":"root_ui","attributes":{"margin":"8","background":"#888888"}},
            
                {"id":"header_title","type":"text","text":"Main Title","parentId":"header_row","attributes":{"color":"#FFFFFF","margin":"4"}},
                {"id":"header_subtitle","type":"text","text":"Subtitle goes here","parentId":"header_row","attributes":{"color":"#AAAAAA","margin":"4"}},
            
                {"id":"body_column_1","type":"column","parentId":"body_stack","attributes":{"margin":"6","background":"#AAAAFF"}},
                {"id":"body_column_2","type":"column","parentId":"body_stack","attributes":{"margin":"6","background":"#AAFFAA"}},
            
                {"id":"body_c1_text_1","type":"text","text":"Column 1 - Item 1","parentId":"body_column_1","attributes":{"color":"#000000"}},
                {"id":"body_c1_text_2","type":"text","text":"Column 1 - Item 2","parentId":"body_column_1","attributes":{"color":"#000000"}},
                {"id":"body_c2_text_1","type":"text","text":"Column 2 - Item 1","parentId":"body_column_2","attributes":{"color":"#000000"}},
                {"id":"body_c2_text_2","type":"text","text":"Column 2 - Item 2","parentId":"body_column_2","attributes":{"color":"#000000"}},
            
                {"id":"c1_subrow_1","type":"row","parentId":"body_column_1","attributes":{"margin":"4","background":"#FFD700"}},
                {"id":"c1_subrow_2","type":"row","parentId":"body_column_1","attributes":{"margin":"4","background":"#FFD700"}},
            
                {"id":"c1_subrow_1_item_1","type":"text","text":"Subrow 1 - Item 1","parentId":"c1_subrow_1","attributes":{"color":"#FF0000"}},
                {"id":"c1_subrow_1_item_2","type":"text","text":"Subrow 1 - Item 2","parentId":"c1_subrow_1","attributes":{"color":"#00FF00"}},
                {"id":"c1_subrow_2_item_1","type":"text","text":"Subrow 2 - Item 1","parentId":"c1_subrow_2","attributes":{"color":"#0000FF"}},
                {"id":"c1_subrow_2_item_2","type":"text","text":"Subrow 2 - Item 2","parentId":"c1_subrow_2","attributes":{"color":"#FFFF00"}},
            
                {"id":"nested_stack_1","type":"stack","parentId":"c1_subrow_1_item_1","attributes":{"background":"#AA0000","margin":"3"}},
                {"id":"nested_stack_1_text","type":"text","text":"Inside nested stack","parentId":"nested_stack_1","attributes":{"color":"#FFFFFF"}},
            
                {"id":"footer_left","type":"text","text":"Footer Left","parentId":"footer_row","attributes":{"color":"#FFFFFF"}},
                {"id":"footer_right","type":"text","text":"Footer Right","parentId":"footer_row","attributes":{"color":"#FFFFFF"}}
            ]
        """.trimIndent()
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyWidgetTheme {
            AddWidgetButtons()
        }
    }
}