package com.guahoo.data.db.model

import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.Track

// TrackEntity to Track
fun TrackEntity.toDomainModel(): Track {
    return Track(
        id = id,
        groupId = groupId,
        nodes = nodes?.map { it.toDomainModel() },  // Convert NodeEntity to Node
        tags = tags,
        color = color,
    )
}

// Track to TrackEntity
fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = id,
        nodes = nodes?.map { it.toEntity() },  // Convert Node to NodeEntity
        tags = tags,
        groupId = groupId,
        color = color,
    )
}




// NodeEntity to Node
fun NodeEntity.toDomainModel(): Node {
    return Node(
        id = id,
        type = type,
        lat = lat,
        lon = lon
    )
}

// Node to NodeEntity
fun Node.toEntity(): NodeEntity {
    return NodeEntity(
        id = id,
        type = type,
        lat = lat,
        lon = lon
    )
}
