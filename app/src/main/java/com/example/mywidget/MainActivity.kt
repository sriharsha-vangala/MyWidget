package com.example.mywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mywidget.clock.ClockUpdateService
import com.example.mywidget.clock.MyClockWidgetReceiver
import com.example.mywidget.clock.datastore.storeWidgetDimens
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
        var height by remember { mutableStateOf("") }
        var width by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 100.dp, 16.dp, 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = {
                        Text("Height")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = {
                        Text("Width")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val heightValue = height.toIntOrNull()
                        val widthValue = width.toIntOrNull()

                        if (heightValue == null || heightValue <= 0 || heightValue > 200) {
                            Toast.makeText(
                                this@MainActivity,
                                "Please enter a valid height Range 1-200",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (widthValue == null || widthValue <= 0 || widthValue > 200) {
                            Toast.makeText(
                                this@MainActivity,
                                "Please enter a valid width Range 1-200",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        storeWidgetDimens(this@MainActivity, heightValue, widthValue)
                        requestWidgetPin()
                        val startIntent = Intent(this@MainActivity, ClockUpdateService::class.java)
                        this@MainActivity.startForegroundService(startIntent)
                    }
                ) {
                    Text("Add Widget to Home Screen")
                }
            }
        }
    }

    private fun requestWidgetPin() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, MyClockWidgetReceiver::class.java)
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