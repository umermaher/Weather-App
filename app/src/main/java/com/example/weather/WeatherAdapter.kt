package com.example.weather

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(private val context: Context, private val weatherList:List<Weather>):
    RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.weather_item,null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = weatherList.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val timeText: TextView =itemView.findViewById(R.id.cardTime)
        private val tempText: TextView =itemView.findViewById(R.id.cardTemp)
        private val windText: TextView =itemView.findViewById(R.id.cardWind)
        private val icon: ImageView =itemView.findViewById(R.id.cardIcon)
        private val cardLayout:LinearLayout=itemView.findViewById(R.id.cardLayout)

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(position: Int) {
            val weather=weatherList[position]
            val temp=weather.temp.toFloat().toInt()
            val wind=weather.wind.toFloat().toInt()
            tempText.text="$temp°"
            windText.text="$wind km/h"
            Picasso.get().load("http:${weather.iconUrl}").into(icon)
            val input=SimpleDateFormat("yyyy-MM-dd hh:mm")
            val output=SimpleDateFormat("hh:mm aa")
            try{
                val date=input.parse(weather.time)
                timeText.text= output.format(date)
            }catch (e:Exception){
                e.printStackTrace()
            }
            if(weather.isDay==1) cardLayout.background= context.resources.getDrawable(R.color.day)
            else cardLayout.background= context.resources.getDrawable(R.color.night)
        }
    }
}