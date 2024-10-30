package com.guahoo.data.response

import android.util.Log
import com.google.gson.annotations.SerializedName


// Root class for the entire JSON response
data class OverpassResponse(
    @SerializedName("version") val version: Double = 0.0,
    @SerializedName("generator") val generator: String = "",
    @SerializedName("osm3s") val osm3s: OSM3S = OSM3S("",""),
    @SerializedName("elements") val elements: List<Element> = listOf<Element>()
)


data class OSM3S(
    @SerializedName("timestamp_osm_base") val timestampOsmBase: String,
    @SerializedName("copyright") val copyright: String
)


sealed class Element {
    data class Relation(
        @SerializedName("type") val type: String,
        @SerializedName("id") val id: Long,
        @SerializedName("members") val members: List<Member>,
        @SerializedName("tags") val tags: RelationTags
    ) : Element()

    data class Node(
        @SerializedName("type") val type: String,
        @SerializedName("id") val id: Long,
        @SerializedName("lat") val latitude: Double,
        @SerializedName("lon") val longitude: Double
    ) : Element()

    data class Way(
        @SerializedName("type") val type: String,
        @SerializedName("id") val id: Long,
        @SerializedName("nodes") val nodes: List<Long> // List of node references
    ) : Element()
}

// Class for Relation members (nodes and ways)
sealed class Member {
    data class NodeMember(
        @SerializedName("type") val type: String,
        @SerializedName("ref") val ref: Long,
        @SerializedName("role") val role: String
    ) : Member()

    data class WayMember(
        @SerializedName("type") val type: String,
        @SerializedName("ref") val ref: Long,
        @SerializedName("role") val role: String
    ) : Member()

    data class RelationMember(
        @SerializedName("type") val type: String,
        @SerializedName("id") val id: Long,
        @SerializedName("members") val members: List<Member>,
        @SerializedName("tags") val tags: RelationTags
    ): Member()
}

// Tags for the relation element
data class RelationTags(
    @SerializedName("description") val description: String?,
    @SerializedName("network") val network: String?,
    @SerializedName("network:type") val networkType: String?,
    @SerializedName("osmc:symbol") val osmcSymbol: String?,
    @SerializedName("route") val route: String,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("name:en") val nameEn: String?,
    @SerializedName("website") val website: String?,
)

fun RelationTags.toMap(): Map<String, String> {
    Log.d("ToDBTracks", "$description $name $osmcSymbol")
    return mutableMapOf<String, String>().apply {

        description?.let { put("description", it) }
        network?.let { put("network", it) }
        networkType?.let { put("network:type", it) }
        put("route", route)  // Non-nullable fields are added directly
        put("type", type)
        put("name", name)
        osmcSymbol?.let { put("osmc:symbol", it) }
        nameEn?.let { put("name:en", it) }
        website?.let { put("website", it) }
    }
}





