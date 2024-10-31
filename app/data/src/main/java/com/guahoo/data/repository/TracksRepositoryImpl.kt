package com.guahoo.data.repository

import android.graphics.Color

import android.util.Log
import androidx.room.Transaction
import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.db.model.toDomainModel
import com.guahoo.data.db.model.toEntity
import com.guahoo.data.mapper.mapListToDomain
import com.guahoo.data.mapper.mapToTrack
import com.guahoo.data.network.L
import com.guahoo.data.network.TracksApiService
import com.guahoo.data.preferenses.PreferencesService
import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.Node
import com.guahoo.domain.entity.OverpassElement
import com.guahoo.domain.entity.RelationModel
import com.guahoo.domain.entity.Track
import com.guahoo.domain.entity.Way
import com.guahoo.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import kotlin.random.Random

class TracksRepositoryImpl(
    private val tracksApiService: TracksApiService,
    private val trackDao: TrackDao,
    private val preferencesService: PreferencesService
) : TracksRepository {

    override fun getAllTracks(): List<Track> {
        return trackDao.getAllTracks().map { it.toDomainModel() }
    }

    @Transaction
    suspend fun insertTracksWithTransaction(tracks: List<Track>) {
        tracks.forEach { insertTrackOrUpdateTrack(it) }
    }


    override suspend fun insertTrackOrUpdateTrack(track: Track) {
        val existingTrack = trackDao.getTrackById(track.id)
        val trackEntity = track.toEntity()
        if (existingTrack != null) {
            trackDao.updateTrack(trackEntity)
        } else {
            trackDao.insertTrack(trackEntity)
        }
    }

    override fun fetchTracks(): Flow<ResultState<List<Track>>> = flow {
        val overpassQuery = createOverpassQuery()

        try {
            if (preferencesService.trackIsDownloaded.isNullOrEmpty()) {
                emit(ResultState.Loading("Downloading tracks"))
                val response = tracksApiService.getTracksByArea(overpassQuery)
                emit(ResultState.Loading("Mapping tracks1"))
                val elements = response.elements
                emit(ResultState.Loading("Mapping tracks"))

                val trackList = processTracks(elements.mapListToDomain())
                emit(ResultState.Loading("Insert tracks to DB"))

                try {
                    insertTracksWithTransaction(
                        trackList
                    )
                } catch (e: Exception) {
                    com.guahoo.data.network.L.d("REPOS ${e.message}")
                }

            }

            val tracksFromDb = getAllTracks()

            emit(ResultState.Loading("get Tracks from DB"))
            preferencesService.trackIsDownloaded = System.currentTimeMillis().toString()

            emit(ResultState.Success(tracksFromDb))

        } catch (e: HttpException) {
            emit(ResultState.Error("An unexpected error occurred HTTP: ${e.message}"))
        } catch (e: IOException) {
            emit(ResultState.Error("Couldn't reach server, check your internet connection ${e.message}"))
        } catch (e: Exception) {
            emit(ResultState.Error("An unexpected error occurred: ${e}"))
        }
    }
//    relation["route"="hiking"]["osmc:symbol"="red:white:red_bar"](41.051, 40.992, 43.585, 47.316);
//    relation["route"="hiking"]["osmc:symbol"="blue:white:blue_bar"](41.051, 40.992, 43.585, 47.316);
//    relation["route"="hiking"]["osmc:symbol"="yellow:white:yellow_bar"](41.051, 40.992, 43.585, 47.316);


    private fun createOverpassQuery(): String = """
           [out:json][timeout:25];
// Get the relation that represents the boundary of Georgia
area["ISO3166-1"="GE"][boundary=administrative][admin_level=2]->.searchArea;

// Find all hiking routes within Georgia's boundaries
(
  relation
    ["route"="hiking"]["type"="route"]
    ["name"~"Trail|trail|hike|trails|Trails|Hike|Track|track"]
   (area.searchArea);

  relation
    ["route"="hiking"]["osmc:symbol"="blue:white:blue_bar"]
    (area.searchArea);

  relation
    ["route"="hiking"]["osmc:symbol"="red:white:red_bar"]
    (area.searchArea);  

    relation
    ["route"="foot"]["osmc:symbol"="red:white:red_bar"]
    (area.searchArea);

  relation
    ["route"="hiking"]["osmc:symbol"="yellow:white:yellow_bar"]
    (area.searchArea);  

    relation
    ["route"="foot"]["osmc:symbol"="yellow:white:yellow_bar"]
    (area.searchArea);
);
out body;
>;
out skel qt;

    """.trimIndent()

    private fun processTracks(tracksData: List<OverpassElement>): List<Track> {
        // Create maps for fast lookup
        val waysMap = tracksData.filterIsInstance<Way>().associateBy { it.id }
        val nodesMap = tracksData.filterIsInstance<Node>().associateBy { it.id }

        // Process relations concurrently
        return tracksData
            .filterIsInstance<RelationModel>()
            .map { relation ->
                val trackList = processRelation(relation, waysMap, nodesMap)
                trackList
            }
    }


    private fun processRelation(
        relation: RelationModel,
        waysMap: Map<Long, Way>,
        nodesMap: Map<Long, Node>
    ): Track {
        val relationWayIds = relation.waysId ?: listOf()
        val relationColor = Color.parseColor(getRandomColor())  // Generate color once

        val nodeList = mutableListOf<Node>()

        relationWayIds
            .mapNotNull { waysMap[it] }  // Get ways by id from map
            .forEach { way ->
                val wayNodes = way.nodes ?: return@forEach  // Skip if no nodes

                // Map the node IDs to actual Node objects
                val trackPoints = wayNodes.mapNotNull { nodeId -> nodesMap[nodeId] }



                if (nodeList.isNotEmpty()) {
                    if (nodeList.last().id == trackPoints.first().id) {
                        nodeList.addAll(trackPoints)
                    } else if (nodeList.last().id == trackPoints.last().id) {
                        nodeList.addAll(trackPoints.reversed())

                    } else if (nodeList.last().id != trackPoints.first().id) {
                        // Проверяем, есть ли во втором списке точка с id, уже присутствующим в первом списке
                        val matchingPointIndex = trackPoints.indexOfFirst { point ->
                            nodeList.any { it.id == point.id }
                        }

                        if (matchingPointIndex != -1) {
                            // Если повторяющаяся точка найдена, берем из первого листа точки от этой точки до конца
                            val matchingId = trackPoints[matchingPointIndex].id
                            val newSubList = nodeList.dropWhile { it.id != matchingId }

                            // Реверсируем новый подсписок и добавляем его к первому листу
                            nodeList.addAll(newSubList.reversed())
                            nodeList.addAll(trackPoints)
                        } else {
                            nodeList.addAll(trackPoints)
                        }


                    }

                } else {
                    nodeList.addAll(trackPoints)
                }
            }

        return Track(
            nodes = nodeList,
            groupId = relation.id,
            tags = relation.tags,
            id = relation.id,
            color = relationColor
        )
    }


    private fun getRandomColor(): String {
        val red = Random.nextInt(128)
        val green = Random.nextInt(128)
        val blue = Random.nextInt(128)

        return "#%02X%02X%02X".format(red, green, blue)
    }
}



