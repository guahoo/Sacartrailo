package com.guahoo.presentation.ui.base

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.guahoo.app.presentation.R
import com.guahoo.data.mapper.Extensions.haversineDistance
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.Track
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import kotlin.math.roundToInt

class BaseScreen {
    companion object {

        @Composable
        fun InitialScreen() {
            DisplayWallpaper()
        }

        @Composable
        fun LoadingScreen() {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Loading trails")
                }

                DisplayWallpaper(alpha = 0.5f)
            }
        }

        @Composable
        fun ErrorScreen(message: String) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }

        @Composable
        fun TrackMapView(modifier: Modifier = Modifier, tracks: List<Track>) {
            val selectedTrack = remember { mutableStateOf<Marker?>(null) }
            val selectedPolylines = mutableListOf<Polyline>()

            OSMDroidMapView(modifier) {
                overlayManager.clear()

                tracks.forEach { track ->
                    addTrackPolyline(track, selectedPolylines)
                }

                handleMapEvents(selectedPolylines)
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
                        controller.setZoom(10.0)
                        controller.setCenter(GeoPoint(42.000, 43.500, 0.0))
                        isTilesScaledToDpi = true
                        mapViewConfig(this)
                    }
                },
                update = { mapViewConfig(it) }
            )
        }

        private fun MapView.addTrackPolyline(track: Track, selectedPolylines: MutableList<Polyline>) {
            val polyline = Polyline().apply {
                id = track.groupId.toString()
                title = track.tags?.get("name")
                color = track.color ?: com.guahoo.data.R.color.black
                width = 10.0f
                alpha = 0.8f
                outlinePaint.strokeCap = Paint.Cap.ROUND
                outlinePaint.isAntiAlias = true
                setPoints(track.nodes?.mapToGeoPoint())
            }

            polyline.setOnClickListener { _, _, _ ->
                handlePolylineClick(this, polyline, track, selectedPolylines)
                true
            }

            overlays.add(polyline)
        }

        private fun MapView.handlePolylineClick(
            mapView: MapView,
            polyline: Polyline,
            track: Track,
            selectedPolylines: MutableList<Polyline>
        ) {
            val trackList = mapView.overlays.filterIsInstance<Polyline>()
                .filter { it.id == track.groupId.toString() }

            val geoPoints = trackList.flatMap { it.actualPoints }
            resetSelectedPolylines(mapView, selectedPolylines)

            val distance = calculateTotalDistance(geoPoints)
            addStartEndMarkers(mapView, track, geoPoints, distance)

            highlightTrack(trackList, selectedPolylines)
            mapView.invalidate()
        }

        private fun resetSelectedPolylines(mapView: MapView, selectedPolylines: MutableList<Polyline>) {
            selectedPolylines.forEach { polyline ->
                polyline.width = 10.0f
                polyline.outlinePaint.strokeWidth = 10.0f
            }
            mapView.overlays.filterIsInstance<Marker>().forEach { mapView.overlays.remove(it) }
            selectedPolylines.clear()
        }

        private fun MapView.addStartEndMarkers(mapView: MapView, track: Track, geoPoints: List<GeoPoint>, distance: Int) {
            val startMarker = createMarker(mapView, geoPoints.first(), track.tags, distance, context = this.context)
            val endMarker = createMarker(mapView, geoPoints.last(), track.tags, distance, end = true, context = this.context)

            overlays.add(startMarker)
            overlays.add(endMarker)
        }

        private fun createMarker(
            mapView: MapView,
            position: GeoPoint,
            tags: Map<String, String>?,
            distance: Int,
            end: Boolean = false,
            context: Context
        ): Marker {
            return Marker(mapView).apply {
                this.position = position
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = tags?.get("name")
                icon = ContextCompat.getDrawable(
                    context, if (end) R.drawable.b_marker else R.drawable.a_marker
                )
                setInfoWindow(CustomInfoWindow(mapView, title ?: "", distance.toString(), tags?.get("osmc:symbol")))
            }
        }

        private fun highlightTrack(trackList: List<Polyline>, selectedPolylines: MutableList<Polyline>) {
            trackList.forEach { polyline ->
                polyline.width = 16.0f
                polyline.outlinePaint.strokeWidth = 16.0f
            }
            selectedPolylines.addAll(trackList)
        }

        private fun MapView.handleMapEvents(selectedPolylines: MutableList<Polyline>) {
            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let { Log.d("MapClick", "Clicked at: Lat: ${it.latitude}, Lng: ${it.longitude}") }
                    resetSelectedPolylines(this@handleMapEvents, selectedPolylines)
                    InfoWindow.closeAllInfoWindowsOn(this@handleMapEvents)
                    return false
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    p?.let { Log.d("MapLongPress", "Long-pressed at: Lat: ${it.latitude}, Lng: ${it.longitude}") }
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
        private fun DisplayWallpaper(alpha: Float = 1f) {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_beta),
                contentDescription = "wallpapers",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alpha),
                contentScale = ContentScale.Crop
            )
        }

        class CustomInfoWindow(
            mapView: MapView,
            private val title: String,
            private val distance: String,
            private val symbol: String?
        ) : MarkerInfoWindow(R.layout.custom_info_window, mapView) {

            override fun onOpen(item: Any?) {
                mView.findViewById<TextView>(R.id.title_text).apply {
                    text = title
                }
                mView.findViewById<TextView>(R.id.info_text).apply {
                    text = "Distance: $distance km"
                }

                val imageRes: Int? = when(symbol){
                    "blue:white:blue_bar" -> {R.drawable.blue_marker}
                    "red:white:red_bar" -> {R.drawable.red_marker}
                    "yellow:white:yellow_bar" -> {R.drawable.yellow_marker}
                    else -> null
                }

                imageRes?.let {
                    mView.findViewById<ImageView>(R.id.info_image)
                        .setImageResource(it) // Set your image here
                }
            }

            override fun onClose() {}
        }
    }
}
