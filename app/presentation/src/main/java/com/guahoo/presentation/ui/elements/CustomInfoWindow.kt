package com.guahoo.presentation.ui.elements

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.guahoo.app.presentation.R
import com.guahoo.domain.entity.Track
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class CustomInfoWindow(
    mapView: MapView,
    private val track: Track,
    private val title: String,
    private val distance: String,
    private val symbol: String?,
    private val onInfoWindowClick: (Track) -> Unit
) : MarkerInfoWindow(R.layout.custom_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val layout = mView.findViewById<LinearLayout>(R.id.clickable_layout)

        mView.findViewById<TextView>(R.id.title_text).apply {
            text = title
        }
        mView.findViewById<TextView>(R.id.info_text).apply {
            text = "Distance: $distance km"
        }

        val imageRes: Int? = when (symbol) {
            "blue:white:blue_bar" -> {
                R.drawable.blue_marker
            }

            "red:white:red_bar" -> {
                R.drawable.red_marker
            }

            "yellow:white:yellow_bar" -> {
                R.drawable.yellow_marker
            }

            else -> null
        }

        imageRes?.let {
            mView.findViewById<ImageView>(R.id.info_image)
                .setImageResource(it)
        }


        layout.setOnClickListener {
            onInfoWindowClick(track)
        }
    }

    override fun onClose() {}
}

