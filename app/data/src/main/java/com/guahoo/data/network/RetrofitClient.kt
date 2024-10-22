package com.guahoo.data.network


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.guahoo.data.BuildConfig
import com.guahoo.data.response.Element
import com.guahoo.data.response.Member
import com.guahoo.domain.entity.NODE
import com.guahoo.domain.entity.RELATION
import com.guahoo.domain.entity.WAY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val BASE_TRACK_URL = "https://overpass-api.de/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Element::class.java, ElementDeserializer())
        .registerTypeAdapter(Member::class.java, MemberDeserializer())
        .create()


    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(ApiKeyInterceptor(BuildConfig.OPENWEATHER_API_KEY))
        .build()

    private val trackClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
        .readTimeout(10, TimeUnit.SECONDS) // Set read timeout
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
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Element {
        val jsonObject = json.asJsonObject
        return when (val type = jsonObject["type"].asString) {
            RELATION -> context.deserialize(json, Element.Relation::class.java)
            NODE -> context.deserialize(json, Element.Node::class.java)
            WAY -> context.deserialize(json, Element.Way::class.java)
            else -> throw JsonParseException("Unknown element type: $type")
        }
    }
}

// Custom deserializer for Member sealed class
class MemberDeserializer : JsonDeserializer<Member> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Member {
        val jsonObject = json.asJsonObject
        return when (val type = jsonObject["type"].asString) {
            NODE -> context.deserialize(json, Member.NodeMember::class.java)
            WAY -> context.deserialize(json, Member.WayMember::class.java)
            RELATION -> context.deserialize(json, Member.RelationMember::class.java)
            else -> throw JsonParseException("Unknown member type: $type")
        }
    }
}



class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val url = originalRequest.url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .build()

        return chain.proceed(originalRequest.newBuilder().url(url).build())
    }
}