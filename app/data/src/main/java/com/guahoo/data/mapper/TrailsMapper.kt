package com.guahoo.data.mapper

import com.guahoo.data.response.Element
import com.guahoo.data.response.Element.Relation
import com.guahoo.data.response.Member
import com.guahoo.data.response.toMap
import com.guahoo.domain.entity.NODE
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.OverpassElement
import com.guahoo.domain.entity.RELATION
import com.guahoo.domain.entity.RelationModel
import com.guahoo.domain.entity.Track
import com.guahoo.domain.entity.WAY


fun Relation.toTrailsDomain(): RelationModel {
    return RelationModel(
        waysId = members.filterIsInstance<Member.WayMember>().map { it.ref },
        id = id,
        type = RELATION,
        tags = this.tags.toMap()
    )
}

fun Element.Way.toWayTrailsDomain(): com.guahoo.domain.entity.Way {
    return com.guahoo.domain.entity.Way(
        nodes = this.nodes,
        id = this.id ?: 0L,
        type = WAY,
        tags = listOf()
    )
}

fun List<Node>.mapToTrack(id: Long, color: Int?, groupId: Long, tags: Map<String, String>): Track {
    return Track(
        nodes = this,
        id = id,
        groupId = groupId,
        color = color,
        tags = tags,
    )
}


fun Element.Node.toDomain(): Node {
    return Node(
        id = this.id,
        type = NODE,
        lat = this.latitude,
        lon = this.longitude
    )
}


fun List<Element>.mapListToDomain(): List<OverpassElement> {
    return this.map {
        when (it) {
            is Element.Node -> it.toDomain()
            is Relation -> it.toTrailsDomain()
            is Element.Way -> it.toWayTrailsDomain()
        }
    }
}



