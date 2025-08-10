package com.example.mywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.mywidget.clock.ClockUpdateService
import com.example.mywidget.clock.MyClockWidgetReceiver1x1
import com.example.mywidget.clock.MyClockWidgetReceiver1x2
import com.example.mywidget.clock.MyClockWidgetReceiver1x3
import com.example.mywidget.clock.MyClockWidgetReceiver1x4
import com.example.mywidget.clock.MyClockWidgetReceiver2x1
import com.example.mywidget.clock.MyClockWidgetReceiver2x2
import com.example.mywidget.clock.MyClockWidgetReceiver2x3
import com.example.mywidget.clock.MyClockWidgetReceiver2x4
import com.example.mywidget.clock.MyClockWidgetReceiver3x1
import com.example.mywidget.clock.MyClockWidgetReceiver3x2
import com.example.mywidget.clock.MyClockWidgetReceiver3x3
import com.example.mywidget.clock.MyClockWidgetReceiver3x4
import com.example.mywidget.clock.MyClockWidgetReceiver4x1
import com.example.mywidget.clock.MyClockWidgetReceiver4x2
import com.example.mywidget.clock.MyClockWidgetReceiver4x3
import com.example.mywidget.clock.MyClockWidgetReceiver4x4
import com.example.mywidget.ui.theme.MyWidgetTheme

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWidgetTheme {
                AddWidgetToHomeScreenUI()
            }
        }
    }

    @Composable
    fun AddWidgetToHomeScreenUI() {
       val gridOptions = (1..4).flatMap { h -> (1..4).map { w -> "${h}x${w}" } }
        var expanded by remember { mutableStateOf(false) }
        var selectedGrid by remember { mutableStateOf(gridOptions[0]) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 100.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
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
                                    modifier = Modifier.clickable { expanded = true }
                                )
                            },
                            modifier = Modifier.width(200.dp)
                                .clickable { expanded = true },
                            colors = OutlinedTextFieldDefaults.colors().copy(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        gridOptions.forEach { grid ->
                            DropdownMenuItem(text = { Text(grid) }, onClick = {
                                selectedGrid = grid
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val (heightValue, widthValue) = selectedGrid.split("x").map { it.toInt() }
                        requestWidgetPin(heightValue, widthValue)
                        val startIntent = Intent(this@MainActivity, ClockUpdateService::class.java)
                        this@MainActivity.startForegroundService(startIntent)
                    }) {
                    Text("Add Widget to Home Screen")
                }
            }
        }
    }

    private fun requestWidgetPin(height: Int, width: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val providerClass = when {
            height == 1 && width == 1 -> MyClockWidgetReceiver1x1::class.java
            height == 1 && width == 2 -> MyClockWidgetReceiver1x2::class.java
            height == 1 && width == 3 -> MyClockWidgetReceiver1x3::class.java
            height == 1 && width == 4 -> MyClockWidgetReceiver1x4::class.java
            height == 2 && width == 1 -> MyClockWidgetReceiver2x1::class.java
            height == 2 && width == 2 -> MyClockWidgetReceiver2x2::class.java
            height == 2 && width == 3 -> MyClockWidgetReceiver2x3::class.java
            height == 2 && width == 4 -> MyClockWidgetReceiver2x4::class.java
            height == 3 && width == 1 -> MyClockWidgetReceiver3x1::class.java
            height == 3 && width == 2 -> MyClockWidgetReceiver3x2::class.java
            height == 3 && width == 3 -> MyClockWidgetReceiver3x3::class.java
            height == 3 && width == 4 -> MyClockWidgetReceiver3x4::class.java
            height == 4 && width == 1 -> MyClockWidgetReceiver4x1::class.java
            height == 4 && width == 2 -> MyClockWidgetReceiver4x2::class.java
            height == 4 && width == 3 -> MyClockWidgetReceiver4x3::class.java
            height == 4 && width == 4 -> MyClockWidgetReceiver4x4::class.java
            else -> MyClockWidgetReceiver2x2::class.java
        }
        val provider = ComponentName(this, providerClass)
        appWidgetManager.requestPinAppWidget(provider, null, null)
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyWidgetTheme {
            AddWidgetToHomeScreenUI()
        }
    }
}