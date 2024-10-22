package com.guahoo.data.mapper

import android.content.Context
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
//import com.guahoo.app.presentation.R
import kotlin.random.Random

object Extensions {
 //   fun getRandomColor(context: Context): Int {
        // Определяем доступные цвета
//        val colors = listOf(
//            R.color.purple_100,
//            R.color.purple_200,
//            R.color.purple_300,
//            R.color.purple_400,
//            R.color.purple_500,
//            R.color.purple_600,
//            R.color.purple_700,
//            R.color.purple_800,
//            R.color.purple_900,
//            R.color.teal_100,
//            R.color.teal_200,
//            R.color.teal_300,
//            R.color.teal_400,
//            R.color.teal_500,
//            R.color.teal_600,
//            R.color.teal_700,
//            R.color.teal_800,
//            R.color.teal_900,
//            R.color.lavender_100,
//            R.color.lavender_200,
//            R.color.lavender_300,
//            R.color.lavender_400,
//            R.color.lavender_500,
//            R.color.lavender_600,
//            R.color.lavender_700,
//            R.color.lavender_800,
//            R.color.lavender_900,
//            R.color.cyan_100,
//            R.color.cyan_200,
//            R.color.cyan_300,
//            R.color.cyan_400,
//            R.color.cyan_500,
//            R.color.cyan_600,
//            R.color.cyan_700,
//            R.color.cyan_800,
//            R.color.cyan_900,
//            R.color.indigo_100,
//            R.color.indigo_200,
//            R.color.indigo_300,
//            R.color.indigo_400,
//            R.color.indigo_500,
//            R.color.indigo_600,
//            R.color.indigo_700,
//            R.color.indigo_800,
//            R.color.indigo_900,
//            R.color.violet_100,
//            R.color.violet_200,
//            R.color.violet_300,
//            R.color.violet_400,
//            R.color.violet_500,
//            R.color.violet_600,
//            R.color.violet_700,
//            R.color.violet_800,
//            R.color.violet_900,
//            R.color.light_purple,
//            R.color.dark_teal
//        )

        // Случайным образом выбираем один из цветов
//        val randomIndex = Random.nextInt(colors.size)
//        val selectedColorResId = colors[randomIndex]

        // Возвращаем цвет с помощью ContextCompat
      //  return ContextCompat.getColor(context, selectedColorResId)

    private const val EARTH_RADIUS = 6371.0 // Radius of the Earth in kilometers

    // Function to calculate distance between two coordinates using Haversine formula
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c // distance in kilometers
    }
}

//}