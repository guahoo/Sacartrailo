package com.guahoo.data.network


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.guahoo.data.response.Element
import com.guahoo.data.response.Member
import com.guahoo.data.response.OSM3S
import com.guahoo.data.response.OverpassResponse
import com.guahoo.domain.entity.NODE
import com.guahoo.domain.entity.RELATION
import com.guahoo.domain.entity.WAY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {

    private const val BASE_TRACK_URL = "https://overpass-api.de/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Element::class.java, ElementDeserializer())
        .registerTypeAdapter(Member::class.java, MemberDeserializer())
       // .registerTypeAdapter(OverpassResponse::class.java, OverpassResponseDeserializer())
      //  .registerTypeAdapter(OSM3S::class.java, OSM3Deserializer())
        .create()


    private val trackClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Set read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Set write timeout
        .build()


    @Provides
    @Singleton
    fun provideTracksApiService(): TracksApiService =
        Retrofit.Builder()
            .baseUrl(BASE_TRACK_URL)
            .client(trackClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TracksApiService::class.java)
}


class ElementDeserializer : JsonDeserializer<Element> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Element {
        val jsonObject = json.asJsonObject
     //   L.d(".v(\"JSON_ELEM")
        val type = jsonObject["type"]?.asString ?: throw JsonParseException("Missing type field")
        return when (type) {
            RELATION -> context.deserialize(json, Element.Relation::class.java)
            NODE -> context.deserialize(json, Element.Node::class.java)
            WAY -> context.deserialize(json, Element.Way::class.java)
            else -> throw JsonParseException("Unknown element type: $type")
        }
    }
}

// Custom deserializer for Member sealed class
class MemberDeserializer : JsonDeserializer<Member> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Member {
        val jsonObject = json.asJsonObject

        val type = jsonObject["type"]?.asString ?: throw JsonParseException("Missing type field")

        return when (type) {
            NODE -> context.deserialize(json, Member.NodeMember::class.java)
            WAY -> context.deserialize(json, Member.WayMember::class.java)
            RELATION -> context.deserialize(json, Member.RelationMember::class.java)
            else -> throw JsonParseException("Unknown member type: $type")
        }
    }
}

class OverpassResponseDeserializer : JsonDeserializer<OverpassResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OverpassResponse {
        val jsonObject = json.asJsonObject
        L.d(".v(\"JSON_ELEM1\" 1")
        val version = jsonObject["version"].asDouble
        L.d(".v(\"JSON_ELEM1\" 2")
        val generator = jsonObject["generator"].asString
        L.d(".v(\"JSON_ELEM1\" 3")
        val osm3s = context.deserialize<OSM3S>(jsonObject["osm3s"], OSM3S::class.java)
        L.d(".v(\"JSON_ELEM1\" 4")
        val elementsType: Type = object : TypeToken<List<Element>>() {}.type
        L.d(".v(\"JSON_ELEM1\" $elementsType")
        val elements = context.deserialize<List<Element>>(jsonObject["elements"], elementsType)
        L.d(".v(\"JSON_ELEM1\" 5")

        return OverpassResponse(version, generator, osm3s, elements)
    }
}


class OSM3Deserializer : JsonDeserializer<OSM3S> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): OSM3S {
        val jsonObject = json.asJsonObject
        val timestampOsmBase = jsonObject["timestamp_osm_base"].asString
        val copyright: String = jsonObject["copyright"].asString
        return OSM3S(timestampOsmBase, copyright)
    }
}
