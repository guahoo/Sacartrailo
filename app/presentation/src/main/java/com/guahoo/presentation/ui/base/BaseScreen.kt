package com.guahoo.presentation.ui.base

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToInt


class BaseScreen {
    companion object{
        @Composable
        fun InitialScreen() {
            Image(
                painter = painterResource(id = R.drawable.wallpaper_beta),
                contentDescription = "wallpapers",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        @Composable
        fun LoadingScreen() {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                Column(Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = "Loading trails"
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.wallpaper_beta),
                    contentDescription = "wallpapers",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.5f),
                    contentScale = ContentScale.Crop,
                    )
            }
        }

        @Composable
        fun ErrorScreen(message: String) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)) {
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
        }

        @Composable
        fun TrackMapView(
            modifier: Modifier = Modifier,
            tracks: List<Track>
        ) {
            Log.v("Track_Count: ","${tracks.map { it.groupId }.size}")
            val selectedPolylines: MutableList<Polyline> = mutableListOf()


            OSMDroidMapView(modifier) {
                overlayManager.clear() // Clear the overlays only once at the beginning

                tracks.map { track ->
                    val polyline = Polyline().apply {
                        id = track.groupId.toString() // Assign ID to each polyline
                        title = track.tags?.get("name")
                        color = track.color ?: com.guahoo.data.R.color.black
                        isGeodesic = true
                        width = 8.0f
                        alpha = 0.8f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        outlinePaint.isAntiAlias = true
                        setPoints(track.nodes?.mapToGeoPoint()) // Convert nodes to GeoPoints
                    }

                    // Add click listener for the polyline
                    polyline.setOnClickListener { _, _, _ ->
                        // Get all polylines with the same groupId
                        val trackList = this.overlays.filterIsInstance<Polyline>().
                        filter { it.id == track.groupId.toString() }


                        val geoPoints = trackList.flatMap { it.actualPoints }

                        val distance = calculateTotalDistance(geoPoints)
                        val startMarker = Marker(this)
                        startMarker.position = geoPoints.first().let { GeoPoint(it?.latitude?:0.0, it?.longitude?:0.0) }
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        startMarker.title = track.tags?.get("name") + "\n dist: $distance km"
                        startMarker.icon = ContextCompat.getDrawable(context, R.drawable.a_marker)
                        this.overlays.add(startMarker)

                        // Add marker at the end of the polyline
                        val endMarker = Marker(this)
                        endMarker.position =  geoPoints.last().let { GeoPoint(it?.latitude?:0.0, it?.longitude?:0.0) }
                        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        endMarker.title = track.tags?.get("name") + "\n dist: $distance km"
                        endMarker.icon = ContextCompat.getDrawable(context, R.drawable.b_marker)
                        this.overlays.add(endMarker)

                        // decrease the width of all previous selected polylines
                        selectedPolylines.forEach { selectedTraks ->
                            selectedTraks.width = 8.0f // New width when clicked
                            selectedTraks.outlinePaint.strokeWidth = 8.0f
                            this.overlays.remove(startMarker)
                            this.overlays.remove(endMarker)
                        }

                        // Increase the width of all matching polylines
                        trackList.forEach { trackPart ->
                            trackPart.width = 16.0f // New width when clicked
                            trackPart.outlinePaint.strokeWidth = 16.0f

                        }

                        selectedPolylines.clear()
                        selectedPolylines.addAll(trackList)


                        this.invalidate() // Redraw the map to apply changes
                        true // Return true to indicate the click was handled
                    }

                    this.overlays.add(polyline)
                }
            }
        }


        @Composable
        fun OSMDroidMapView(
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
                        isTilesScaledToDpi = true
                        controller.setCenter(GeoPoint(42.000, 43.500, 0.0))
                        mapViewConfig(this)
                    }
                },
                update = { mapView -> mapViewConfig(mapView) }
            )
        }


        private fun List<Node>.mapToGeoPoint(): List<GeoPoint> {
            return map { GeoPoint(it.lat, it.lon) }.simplifyTrackWithJTS(0.0001)
        }

        private fun List<GeoPoint>.simplifyTrackWithJTS(tolerance: Double): List<GeoPoint> {
            // Create a GeometryFactory instance
            val geometryFactory = GeometryFactory()

            // Convert GeoPoints to JTS Coordinates
            val coordinates = this.map { Coordinate(it.longitude, it.latitude) }.toTypedArray()

            // Create a LineString from the coordinates
            val lineString: LineString = geometryFactory.createLineString(coordinates)

            // Apply the Douglas-Peucker simplification
            val simplifiedLineString = DouglasPeuckerSimplifier.simplify(lineString, tolerance)

            // Convert simplified LineString back to GeoPoints
            return simplifiedLineString.coordinates.map { coord -> GeoPoint(coord.y, coord.x) }
        }


        private fun calculateTotalDistance(nodes: List<GeoPoint>?): Int {
            if (nodes == null) return 0
            var totalDistance = 0.0

            for (i in 0 until nodes.size - 1) {
                val node1 = nodes[i]
                val node2 = nodes[i + 1]

                totalDistance += haversineDistance(node1.latitude, node1.longitude, node2.latitude, node2.longitude)
            }

            return totalDistance.roundToInt()
        }

    }

}