package com.guahoo.presentation.ui.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.util.Xml
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.guahoo.app.presentation.R
import com.guahoo.data.mapper.Extensions.haversineDistance
import com.guahoo.data.network.L
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.Track
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import kotlin.math.roundToInt


class BaseScreen {
    companion object {

        @Composable
        fun InitialScreen() {
            DisplayWallpaper(alpha = 0.5f, R.drawable.wallpaper_beta)
        }

        @Composable
        fun LoadingScreen(message: String) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message)
                }

                DisplayWallpaper(alpha = 0.5f, R.drawable.wallpaper_beta)
            }
        }

        @Composable
        fun ErrorScreen(message: String) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()

            ) {
                DisplayWallpaper(alpha = 0.8f, R.drawable.error_screen_wallpaper)
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        @Composable
        @Preview
        private fun PreviewMapView() {
            TrackMapView(tracks = listOf())
        }

        @Composable
        fun TrackMapView(modifier: Modifier = Modifier, tracks: List<Track>) {
            val selectedTrack = remember { mutableStateOf<Marker?>(null) }
            var showBottomSheet by remember { mutableStateOf<Track?>(null) }

            val selectedPolylines = mutableListOf<Polyline>()


            val clusterer = RadiusMarkerClusterer(LocalContext.current).apply {
              // Adjust radius as needed
                setMaxClusteringZoomLevel(11)
                setIcon(getBitmapFromDrawable(R.drawable.cluster_icon))
            }


            Box(modifier = modifier) {
                OSMDroidMapView(modifier) {
                    overlayManager.clear()

                    tracks.forEach { track ->
                      addTrackPolyline(track, selectedPolylines,
                            onInfoWindowClick = { selectedTrack ->
                                showBottomSheet = selectedTrack // Set showBottomSheet
                            })
                        if (track.nodes?.isNotEmpty() == true) {
                            val marker = createMarker(
                                this,
                                GeoPoint(track.nodes?.first()?.lat!!, track.nodes?.first()?.lon!!),
                                track,
                                0,
                                context = this.context,
                                infoMarker = true
                            )
                            clusterer.add(marker)
                        }
                    }

                    clusterer
                    handleMapEvents(selectedPolylines)
                    overlayManager.add(clusterer)


                    this.addMapListener(object : MapListener {
                        override fun onZoom(event: ZoomEvent): Boolean {
                            overlays?.filterIsInstance<Polyline>()
                                ?.forEach {
                                    it.isVisible = event.zoomLevel >= 12

                                }

                               //  clusterer.isEnabled = event.zoomLevel <= 11
                            //  this@OSMDroidMapView.invalidate() // Refresh the map to apply changes

                            this@OSMDroidMapView.invalidate()
                            return true
                        }

                        override fun onScroll(event: ScrollEvent?): Boolean {
                            return false
                        }
                    })

                }
            }

            if (showBottomSheet != null) {
                MarkerInfoBottomSheet(
                    track = showBottomSheet!!,
                    context = LocalContext.current,
                    onDismiss = { showBottomSheet = null }
                )
            }
        }


        @Composable
        fun getBitmapFromDrawable(drawableResId: Int): Bitmap? {
            val context = LocalContext.current
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            return drawable?.let {
                val bitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                DrawableCompat.wrap(it).apply {
                    setBounds(0, 0, canvas.width, canvas.height)
                    draw(canvas)
                }
                bitmap
            }
        }

        @Composable
        private fun OSMDroidMapView(
            modifier: Modifier = Modifier,
            mapViewConfig: MapView.() -> Unit = {}
        ) {
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        controller.zoomTo(10)
                        controller.animateTo(GeoPoint(42.2679, 42.7180))
                        isTilesScaledToDpi = true
                        mapViewConfig(this)
                    }
                },
                update = { mapViewConfig(it) }
            )
        }

        private fun MapView.addTrackPolyline(
            track: Track,
            selectedPolylines: MutableList<Polyline>,
            onInfoWindowClick: (Track) -> Unit
        ): Polyline {
            // Create the visible polyline
            val visiblePolyline = Polyline().apply {
                id = "${track.groupId}"
                title = "${track.tags?.get("name")}\n${track.id}"
                color = track.color ?: com.guahoo.data.R.color.black // Fill color
                width = 10.0f // Width of the visible stroke
                //isVisible = false
                // alpha = 0.8f // Optional: Adjust transparency if needed
                outlinePaint.strokeCap = Paint.Cap.ROUND
                // outlinePaint.isAntiAlias = true
                setPoints(track.nodes?.mapToGeoPoint())
            }


            //Create the transparent polyline for click detection
            val clickablePolyline = Polyline().apply {
                id = "${track.groupId}"
                title = "${track.tags?.get("name")}\n${track.id}"
                width = 100.0f // Width of the clickable area
                outlinePaint.color = context.resources.getColor(
                    com.guahoo.data.R.color.transparent,
                    null
                )// Fill color)
                setPoints(track.nodes?.mapToGeoPoint())
            }


            clickablePolyline.setOnClickListener { polyline, _, point ->
                handlePolylineClick(
                    this,
                    polyline,
                    track,
                    selectedPolylines,
                    point,
                    onInfoWindowClick
                )
                true
            }
            overlays.add(visiblePolyline)
            overlays.add(clickablePolyline)



            this.invalidate()
            return visiblePolyline
        }

        private fun MapView.handlePolylineClick(
            mapView: MapView,
            polyline: Polyline,
            track: Track,
            selectedPolylines: MutableList<Polyline>,
            point: GeoPoint,
            onInfoWindowClick: (Track) -> Unit
        ) {
            val trackList = mapView.overlays.filterIsInstance<Polyline>()
                .filter { it.id == track.groupId.toString() }

            val geoPoints = track.nodes?.mapToGeoPoint() ?: listOf()
            resetSelectedPolylines(mapView, selectedPolylines)

            val distance = calculateTotalDistance(geoPoints)
            addStartEndMarkers(mapView, track, geoPoints, distance)

            highlightTrack(trackList, selectedPolylines)

            val marker = createMarker(
                mapView,
                point,
                track = track,
                distance,
                context = this.context,
                infoMarker = true,
                onInfoWindowClick = onInfoWindowClick
            )
            marker.showInfoWindow()
            mapView.overlays.add(marker)
            mapView.invalidate()
        }

        private fun resetSelectedPolylines(
            mapView: MapView,
            selectedPolylines: MutableList<Polyline>
        ) {
            selectedPolylines.forEach { polyline ->
                polyline.width = 10.0f
                polyline.outlinePaint.strokeWidth = 10.0f
            }
            mapView.overlays.filterIsInstance<Marker>().forEach { mapView.overlays.remove(it) }
            selectedPolylines.clear()
        }

        private fun MapView.addStartEndMarkers(
            mapView: MapView,
            track: Track,
            geoPoints: List<GeoPoint>,
            distance: Int
        ): List<Marker> {
            val startMarker =
                createMarker(mapView, geoPoints.first(), track, distance, context = this.context)
            val endMarker = createMarker(
                mapView,
                geoPoints.last(),
                track,
                distance,
                end = true,
                context = this.context
            )

            overlays.add(startMarker)
            overlays.add(endMarker)

            return (listOf(startMarker, endMarker))
        }

        private fun createMarker(
            mapView: MapView,
            position: GeoPoint,
            track: Track,
            distance: Int,
            end: Boolean = false,
            context: Context,
            infoMarker: Boolean = false,
            onInfoWindowClick: ((Track) -> Unit)? = null
        ): Marker {
            return Marker(mapView).apply {
                this.position = position
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                val tags = track.tags
                title = tags?.get("name")

                if (infoMarker) {
                    icon = ContextCompat.getDrawable(
                        context,
                        R.drawable.transparent_marker
                    )
                } else {
                    icon = ContextCompat.getDrawable(
                        context, if (end) R.drawable.b_marker else R.drawable.a_marker
                    )
                }

                setInfoWindow(onInfoWindowClick?.let {
                    CustomInfoWindow(
                        mapView,
                        track = track,
                        title ?: "",
                        distance.toString(),
                        tags?.get("osmc:symbol"),
                        it
                    )
                })

                setOnMarkerClickListener { marker, _ ->
                    marker.showInfoWindow()
                    if (onInfoWindowClick != null) {
                        onInfoWindowClick(track)
                    } // Trigger the callback directly when marker is clicked
                    true
                }
            }
        }


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

            // Лаунчер для создания документа
            val createFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/gpx+xml")
            ) { uri: Uri? ->
                uri?.let {
                    saveGpxToUri(context, it, generateGpxContent(trackNodes))
                    onDismiss() // Закрываем диалог после сохранения
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

        private fun highlightTrack(
            trackList: List<Polyline>,
            selectedPolylines: MutableList<Polyline>
        ) {
            trackList.forEach { polyline ->
                polyline.width = 16.0f
                polyline.outlinePaint.strokeWidth = 16.0f
            }
            selectedPolylines.addAll(trackList)
        }

        private fun MapView.handleMapEvents(selectedPolylines: MutableList<Polyline>) {
            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let {
                        Log.d(
                            "MapClick",
                            "Clicked at: Lat: ${it.latitude}, Lng: ${it.longitude}"
                        )
                    }
                    resetSelectedPolylines(this@handleMapEvents, selectedPolylines)
                    InfoWindow.closeAllInfoWindowsOn(this@handleMapEvents)
                    return false
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let {
                        Log.d(
                            "MapLongPress",
                            "Long-pressed at: Lat: ${it.latitude}, Lng: ${it.longitude}"
                        )
                    }
                    return false
                }
            }

            overlays.add(MapEventsOverlay(mapEventsReceiver))
        }

        private fun calculateTotalDistance(nodes: List<GeoPoint>?): Int {
            if (nodes.isNullOrEmpty()) return 0
            var totalDistance = 0.0
            for (i in 0 until nodes.size - 1) {
                totalDistance += haversineDistance(
                    nodes[i].latitude, nodes[i].longitude,
                    nodes[i + 1].latitude, nodes[i + 1].longitude
                )
            }
            return totalDistance.roundToInt()
        }

        private fun List<Node>.mapToGeoPoint(): List<GeoPoint> {
            return map { GeoPoint(it.lat, it.lon) }.simplifyTrackWithJTS(0.0001)
        }

        private fun List<GeoPoint>.simplifyTrackWithJTS(tolerance: Double): List<GeoPoint> {
            val geometryFactory = GeometryFactory()
            val coordinates = map { Coordinate(it.longitude, it.latitude) }.toTypedArray()
            val lineString: LineString = geometryFactory.createLineString(coordinates)
            val simplifiedLineString = DouglasPeuckerSimplifier.simplify(lineString, tolerance)
            return simplifiedLineString.coordinates.map { GeoPoint(it.y, it.x) }
        }

        @Composable
        private fun DisplayWallpaper(alpha: Float = 1f, drawableResource: Int) {
            Image(
                painter = painterResource(id = drawableResource),
                contentDescription = "wallpapers",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alpha),
                contentScale = ContentScale.Crop
            )
        }


        class CustomInfoWindow(
            mapView: MapView,
            private val track: Track,
            private val title: String,
            private val distance: String,
            private val symbol: String?,
            private val onInfoWindowClick: (Track) -> Unit
        ) : MarkerInfoWindow(R.layout.custom_info_window, mapView) {

            override fun onOpen(item: Any?) {
                val layout = mView.findViewById<LinearLayout>(R.id.clickable_layout)

                mView.findViewById<TextView>(R.id.title_text).apply {
                    text = title
                }
                mView.findViewById<TextView>(R.id.info_text).apply {
                    text = "Distance: $distance km"
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
                    mView.findViewById<ImageView>(R.id.info_image)
                        .setImageResource(it)
                }


                layout.setOnClickListener {
                    onInfoWindowClick(track)
                }
            }

            override fun onClose() {}
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
    }
}
