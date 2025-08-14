package com.example.mywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.mywidget.ui.theme.MyWidgetTheme

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWidgetTheme {
                AddWidgetButtons()
            }
        }
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