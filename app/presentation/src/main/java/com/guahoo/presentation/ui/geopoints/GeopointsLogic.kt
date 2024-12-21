package com.guahoo.presentation.ui.geopoints

import com.guahoo.data.mapper.Extensions.haversineDistance
import com.guahoo.domain.entity.Node
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier
import org.osmdroid.util.GeoPoint
import kotlin.math.roundToInt

fun calculateTotalDistance(nodes: List<GeoPoint>?): Int {
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

fun List<Node>.mapToGeoPoint(): List<GeoPoint> {
    return map { GeoPoint(it.lat, it.lon) }.simplifyTrackWithJTS(0.0001)
}

fun List<GeoPoint>.simplifyTrackWithJTS(tolerance: Double): List<GeoPoint> {
    val geometryFactory = GeometryFactory()
    val coordinates = map { Coordinate(it.longitude, it.latitude) }.toTypedArray()
    val lineString: LineString = geometryFactory.createLineString(coordinates)
    val simplifiedLineString = DouglasPeuckerSimplifier.simplify(lineString, tolerance)
    return simplifiedLineString.coordinates.map { GeoPoint(it.y, it.x) }
}