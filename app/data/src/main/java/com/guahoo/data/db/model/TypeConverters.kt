package com.guahoo.data.db.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NodeConverter {
    @TypeConverter
    fun fromNodeList(nodes: List<NodeEntity>?): String {
        return Gson().toJson(nodes)
    }

    @TypeConverter
    fun toNodeList(nodesString: String): List<NodeEntity>? {
        val listType = object : TypeToken<List<NodeEntity>>() {}.type
        return Gson().fromJson(nodesString, listType)
    }
}

class TagConverter {
    @TypeConverter
    fun fromTagList(tags: Map<String, String>?): String {
        return Gson().toJson(tags)
    }

    @TypeConverter
    fun toTagList(tagsString: String): Map<String, String>? {
        val listType = object : TypeToken<Map<String,String>>() {}.type
        return Gson().fromJson(tagsString, listType)
    }
}

class LongListConverter {
    @TypeConverter
    fun fromLongList(nodes: List<Long>?): String {
        return Gson().toJson(nodes)
    }

    @TypeConverter
    fun toLongList(nodesString: String): List<Long>? {
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(nodesString, listType)
    }
}
