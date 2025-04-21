package com.example.charmcqueen

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private val espBaseUrl = "http://192.168.8.21"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {
            FPVRoverApp(baseUrl = espBaseUrl)
        }
    }

    private fun envoyerCommande(cmd: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$espBaseUrl/motor?direction=$cmd")
                println("$url")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val response = connection.inputStream.bufferedReader().readText()
                println("response ESP32: $response")
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Composable
    fun ESP32StreamView() {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    // Ton URL de flux MJPEG
                    val streamUrl = "http://192.168.8.21:81/stream"
                    val html = """
                    <html>
                    <body style="margin:0; padding:0; background-color:#000;">
                        <img src="$streamUrl" style="width:100%; height:auto;" />
                    </body>
                    </html>
                """.trimIndent()

                    loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            }
        )
    }

    @Composable
    fun FPVRoverApp(baseUrl: String) {
        MaterialTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("FPV Rover", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // MJPEG Stream
                ESP32StreamView()

                Spacer(modifier = Modifier.height(16.dp))

                // Boutons de contrôle
                ControleMoteur(
                    onCommand = { cmd -> envoyerCommande(cmd) }
                )
            }
        }
    }

    @Composable
    fun ControleMoteur(onCommand: (String) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { onCommand("forward") }) {
                Text("⬆️ Forward")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { onCommand("left") }) {
                    Text("⬅️ Left")
                }
                Button(onClick = { onCommand("stop") }) {
                    Text("⏹️ Stop")
                }
                Button(onClick = { onCommand("right") }) {
                    Text("➡️ Right")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onCommand("backward") }) {
                Text("⬇️ Backward")
            }
        }
    }
}
