package com.guahoo.presentation.ui.elements

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Xml
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.guahoo.app.presentation.R
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.Track
import com.guahoo.presentation.ui.geopoints.calculateTotalDistance
import com.guahoo.presentation.ui.geopoints.mapToGeoPoint
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerInfoBottomSheet(track: Track, context: Context, onDismiss: () -> Unit) {
    val title = track.tags?.get("name") ?: "track"
    val description = track.tags?.get("description")
    val weblink = track.tags?.get("website")
    val englishName = track.tags?.get("name:en")
    val symbol = track.tags?.get("osmc:symbol")
    var showSaveGpxScreen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Distance: ${calculateTotalDistance(track.nodes?.mapToGeoPoint())} km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,

                    )
                if (weblink != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = weblink,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                    )
                }
                if (englishName != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = englishName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                    )
                }

                val imageRes: Int? = when (symbol) {
                    "blue:white:blue_bar" -> {
                        R.drawable.blue_marker
                    }

                    "red:white:red_bar" -> {
                        R.drawable.red_marker
                    }

                    "yellow:white:yellow_bar" -> {
                        R.drawable.yellow_marker
                    }

                    else -> null
                }

                imageRes?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Marked by",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Image(
                            modifier = Modifier
                                .height(50.dp)
                                .width(50.dp),
                            painter = painterResource(id = imageRes),
                            contentDescription = ""
                        )
                    }
                }

                if (description != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 8.dp),

                    onClick = {
                        showSaveGpxScreen = true
                    },
                    content = {
                        Text(
                            text = "Download",
                            color = Color(0xFFFFEA00)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C873D), // Change this to your desired background color
                        contentColor = Color.White // Change this to your desired text color
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .height(40.dp) // Adjusted height to better fit text
                        .padding(horizontal = 8.dp), // Optional padding
                    onClick = {
                        val gpxUri = saveGpxFile(
                            context,
                            generateGpxContent(track.nodes ?: listOf()),
                            title
                        )
                        gpxUri?.let {
                            shareGpxFile(context, it)
                        }

                    },
                    content = {
                        Text(text = "Share", color = Color(0xFFFFEA00))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C873D), // Change this to your desired background color
                        contentColor = Color.White // Change this to your desired text color
                    )
                )
            }
        }
    )

    if (showSaveGpxScreen) {
        SaveGpxBottomSheet(
            trackNodes = track.nodes ?: listOf(),
            trackName = title,
            onDismiss = { showSaveGpxScreen = false }
        )
        //  showSaveGpxScreen = false
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveGpxBottomSheet(trackNodes: List<Node>, trackName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gpx+xml")
    ) { uri: Uri? ->
        uri?.let {
            saveGpxToUri(context, it, generateGpxContent(trackNodes))
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Save as gpx file")
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    createFileLauncher.launch("$trackName.gpx") // Запускаем диалог создания файла
                }
            ) {
                Text(text = "Save file")
            }
        }
    }
}

// Function to write the GPX content to the chosen file URI
fun saveGpxToUri(context: Context, uri: Uri, gpxContent: String) {
    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(gpxContent.toByteArray())
    }
}

fun generateGpxContent(nodes: List<Node>): String {
    val writer = StringWriter()
    val xmlSerializer: XmlSerializer = Xml.newSerializer()
    xmlSerializer.setOutput(writer)

    xmlSerializer.startDocument("UTF-8", true)
    xmlSerializer.startTag(null, "gpx")
    xmlSerializer.attribute(null, "version", "1.1")
    xmlSerializer.attribute(null, "creator", "YourAppName")

    xmlSerializer.startTag(null, "trk")
    xmlSerializer.startTag(null, "trkseg")

    nodes.forEach { node ->
        xmlSerializer.startTag(null, "trkpt")
        xmlSerializer.attribute(null, "lat", node.lat.toString())
        xmlSerializer.attribute(null, "lon", node.lon.toString())
        xmlSerializer.endTag(null, "trkpt")
    }

    xmlSerializer.endTag(null, "trkseg")
    xmlSerializer.endTag(null, "trk")
    xmlSerializer.endTag(null, "gpx")
    xmlSerializer.endDocument()

    return writer.toString()
}

fun shareGpxFile(context: Context, gpxUri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/gpx+xml"
        putExtra(Intent.EXTRA_STREAM, gpxUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share GPS Track"))
}

fun saveGpxFile(context: Context, gpxContent: String, trackName: String): Uri? {
    // Save GPX file
    val fileName = "$trackName.gpx"
    val file = File(context.getExternalFilesDir(null), fileName)
    return try {
        FileOutputStream(file).use { output ->
            OutputStreamWriter(output).use { writer ->
                writer.write(gpxContent)
            }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
