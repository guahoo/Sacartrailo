package com.guahoo.presentation.ui.elements

import android.content.Context
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.guahoo.app.presentation.R
import com.guahoo.domain.entity.Track
import com.guahoo.presentation.ui.geopoints.calculateTotalDistance
import com.guahoo.presentation.ui.geopoints.mapToGeoPoint
import com.guahoo.presentation.ui.extensions.getBitmapFromDrawable
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


    val clustered = RadiusMarkerClusterer(LocalContext.current).apply {
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
                    clustered.add(marker)
                }
            }

            handleMapEvents(selectedPolylines)
            overlayManager.add(clustered)


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