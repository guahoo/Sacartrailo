package com.guahoo.data.repository

import android.graphics.Color
import androidx.room.Transaction
import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.db.model.toDomainModel
import com.guahoo.data.db.model.toEntity
import com.guahoo.data.mapper.mapToTrack
import com.guahoo.data.network.TracksApiService
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
    private val trackDao: TrackDao
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
                trackDao.updateTrack(trackEntity.copy(dbid = existingTrack.dbid))
            } else {
                trackDao.insertTrack(trackEntity)
            }
        }

    override fun fetchTracks(): Flow<ResultState<List<Track>>> = flow {
        val overpassQuery = createOverpassQuery()

        try {
//            val response = tracksApiService.getTracksByArea(overpassQuery)
//
//
//            val elements = response.elements
//
//
//            insertTracksWithTransaction(processTracks(elements.mapListToDomain()))

            val tracksFromDb = getAllTracks()

            emit(ResultState.Success(tracksFromDb))

        } catch (e: HttpException) {
            emit(ResultState.Error("An unexpected error occurred HTTP: ${e.message}"))
        } catch (e: IOException) {
            emit(ResultState.Error("Couldn't reach server, check your internet connection ${e.message}"))
        } catch (e: Exception) {
            emit(ResultState.Error("An unexpected error occurred: ${e.message}"))
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
            .flatMap { relation -> processRelation(relation, waysMap, nodesMap) }
    }

    private fun processRelation(
        relation: RelationModel,
        waysMap: Map<Long, Way>,
        nodesMap: Map<Long, Node>
    ): List<Track> {
        val relationWayIds = relation.waysId ?: listOf()
        val relationColor = Color.parseColor(getRandomColor())  // Generate color once

        val trackList = mutableListOf<Track>()

        relationWayIds
            .mapNotNull { waysMap[it] }  // Get ways by id from map
            .forEach { way ->
                val wayNodes = way.nodes ?: return@forEach  // Skip if no nodes

                // Map the node IDs to actual Node objects
                val trackPoints = wayNodes.mapNotNull { nodeId -> nodesMap[nodeId] }

                // Create and add track to the list
                trackPoints.mapToTrack(
                    way.id,
                    relationColor,
                    relation.id,
                    relation.tags ?: mapOf()
                ).let {
                    trackList.add(it)
                }
            }

        return trackList
    }



    private fun getRandomColor(): String {
        val red = Random.nextInt(128)
        val green = Random.nextInt(128)
        val blue = Random.nextInt(128)

        return "#%02X%02X%02X".format(red, green, blue)
    }
}



