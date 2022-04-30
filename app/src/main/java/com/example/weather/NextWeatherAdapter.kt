package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

class NextWeatherAdapter(private val context: Context, private val weatherList:List<NextWeather>) :
    RecyclerView.Adapter<NextWeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.next_weather_item, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = weatherList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.nextDayText)
        private val maxText: TextView = itemView.findViewById(R.id.nextMaxText)
        private val minText: TextView = itemView.findViewById(R.id.nextMinText)
        private val icon: ImageView = itemView.findViewById(R.id.nextIcon)
        private val layout: LinearLayout = itemView.findViewById(R.id.nextLayout)

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(position: Int) {
            val weather = weatherList[position]
            Picasso.get().load("http:${weather.iconUrl}").into(icon)
            dayText.text="${weather.day}day"
            maxText.text = "${weather.max}°"
            minText.text=" / ${weather.min}°"

            if (weather.isDay == 1) layout.background = context.resources.getDrawable(R.color.day)
            else layout.background = context.resources.getDrawable(R.color.night)
        }
    }
}