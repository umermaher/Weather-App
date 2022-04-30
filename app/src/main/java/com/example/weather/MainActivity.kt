package com.example.weather

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.weather.utils.isPermissionGranted
import com.example.weather.utils.requestPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var weatherList: ArrayList<Weather>
    private lateinit var nextWeatherAdapter: NextWeatherAdapter
    private lateinit var nextWeatherList: ArrayList<NextWeather>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        private const val LOCATION_PERMISSION_CODE=1
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //Current
        weatherList = ArrayList()
        weatherAdapter = WeatherAdapter(this, weatherList)
        rvWeather.adapter = weatherAdapter

        //Next
        nextWeatherList = ArrayList()
        nextWeatherAdapter = NextWeatherAdapter(this, nextWeatherList)
        rvNextWeather.setHasFixedSize(true)
        rvNextWeather.layoutManager=LinearLayoutManager(this)
        rvNextWeather.adapter = nextWeatherAdapter

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (isPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurLocation()
        } else {
            requestPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_CODE
            )
        }

        searchBar.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getWeatherInfo(searchBar.text.toString())
                progressBar.visibility=View.VISIBLE
            }
            false
        })
    }

    private fun getWeatherInfo(cityName:String){

        val url="http://api.weatherapi.com/v1/forecast.json?key=42dbade0083544c6908151240222804&q=$cityName&days=7&aqi=yes&alerts=yes"
        cityNameText.text=cityName
        val jsonObjectRequest = @SuppressLint("SetTextI18n")
        object: JsonObjectRequest(Request.Method.GET,url,null,
            {
                parentLayout.visibility= View.VISIBLE
                progressBar.visibility=View.GONE
                weatherList.clear()

                val weatherJsonObject = it.getJSONObject("current")
                val conditionJsonObject = it.getJSONObject("current").getJSONObject("condition")

                val temp:String= weatherJsonObject.getString("temp_c")
                val isDay=weatherJsonObject.getInt("is_day")
                val wind = weatherJsonObject.getString("wind_kph").toFloat().toInt()
                val humidity = weatherJsonObject.getString("humidity").toFloat().toInt()
                val feelsLike = weatherJsonObject.getString("feelslike_c").toFloat().toInt()

                val condText=conditionJsonObject.getString("text")
                val condIcon=conditionJsonObject.getString("icon")

                tempText.text="${temp.toFloat().toInt()}"
                Picasso.get().load("http:$condIcon").into(curConditionIcon)
                conditionText.text=condText
                windText.text="$wind km/h"
                humidityText.text="$humidity %"
                feelsLikeText.text="feels like $feelsLike°"

                //changing background on the basis of time
                if(isDay==1) constraintLayout.background= resources.getDrawable(R.drawable.background_day)
                else constraintLayout.background= resources.getDrawable(R.drawable.background_night)

                //For min max temp and today's forecast w.r.t time
                //we can get further forecasts from forecastJsonObject
                val forecastJsonObject=it.getJSONObject("forecast")
                val forecast0 = forecastJsonObject.getJSONArray("forecastday").get(0) as JSONObject
                val hourArray=forecast0.getJSONArray("hour")

                val min=forecast0.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                val max=forecast0.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                maxText.text = "$max°"
                minText.text=" / $min°"

                for(i in 0 until hourArray.length()){
                    val hourObj=hourArray.getJSONObject(i)
                    val time=hourObj.getString("time")
                    val temp=hourObj.getString("temp_c")
                    val wind=hourObj.getString("wind_kph")
                    val icon=hourObj.getJSONObject("condition").getString("icon")

                    weatherList.add(Weather(time,temp,wind,isDay,icon))
                }
                weatherAdapter.notifyDataSetChanged()

                //Now for next week weather forecast
                val format:DateFormat = SimpleDateFormat("EEEEEEEE", Locale.ENGLISH)
                val calendar = Calendar.getInstance()

                val forecastArray=forecastJsonObject.getJSONArray("forecastday")
                nextWeatherList.clear()

                for (i in 1 until 3) {
                    calendar.add(Calendar.DATE, 1)
                    val day = format.format(calendar.time)
                    Log.e("Days$i", "date :$day")
                    val forecast=forecastArray.get(i) as JSONObject
                    val max=forecast.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                    val min = forecast.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                    val icon=forecast.getJSONObject("day").getJSONObject("condition").getString("icon")

                    nextWeatherList.add(NextWeather(day,max,min,isDay,icon))
                }
                nextWeatherAdapter.notifyDataSetChanged()

            },{
                Log.e("MainActivity",it.message.toString())
                Snackbar.make(this,constraintLayout,"Please enter valid city name.",Snackbar.LENGTH_LONG).show()
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers=HashMap<String,String>()
                headers["User-Agent"]="Mozilla/5.0"
                return headers
            }
        }

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    @SuppressLint("MissingPermission")
    private fun getCurLocation() {
        val location = fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            val cityName=getCityName(location?.latitude,location?.longitude)
            getWeatherInfo(cityName)
        }
    }

    private fun getCityName(latitude: Double?, longitude: Double?): String {
        var cityName="Not found"
        val geocoder=Geocoder(baseContext, Locale.getDefault())
        try{
            val addressList= latitude?.let { longitude?.let { it1 ->
                geocoder.getFromLocation(it,
                    it1,10)
            } }
            for(address in addressList!!){
                if(address!=null){
                    val city=address.locality
//                    cityName=city
                    if(city!=null || !city.equals("")){
                        cityName=city
                    }else{
//                        Toast.makeText(this,"City not found!",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return cityName
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            LOCATION_PERMISSION_CODE -> {
                //if request is empty, the result arrays are empty
                if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //Permission was granted. Do the contacts related task you need to do
                    if (isPermissionGranted(this,android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        getCurLocation()
                    }
                }else{
//                    Permission denied, disable the functionality that depends on this permission
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    finish()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}