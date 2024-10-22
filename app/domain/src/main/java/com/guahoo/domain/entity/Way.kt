package com.guahoo.domain.entity

import com.guahoo.domain.R


data class Track(
    val nodes: List<Node>? = null,
    val id: Long = 0L,
    val groupId: Long,
    val tags: Map<String,String>? = mapOf(),
    val color: Int? = R.color.black
)

data class RelationModel(
    val waysId: List<Long>?,
    override val id: Long = 0L,
    override val type: String = RELATION,
    val tags: Map<String, String>?
): OverpassElement()
//way need only for way class
data class Way(
    val nodes: List<Long>?,
    override val id: Long = 0L,
    override val type: String = WAY,
    val tags: List<String>?
): OverpassElement()

data class Node(
    override val id: Long,
    override val type: String = NODE,
    val lat: Double,
    val lon: Double
): OverpassElement()

abstract class OverpassElement{
    open val id: Long = 0
    open val type: String = ""
}

const val NODE = "node"
const val WAY = "way"
const val RELATION = "relation"