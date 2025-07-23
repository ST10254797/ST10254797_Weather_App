package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private val apiKey = "c2cfb03bac68bbf380f03cdbc32a83e4"
    private val unit = "metric"

    private lateinit var weatherTextView: TextView
    private lateinit var weatherIconImageView: ImageView
    private lateinit var humidityTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var sunriseTextView: TextView
    private lateinit var sunsetTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val cityEditText = findViewById<EditText>(R.id.cityEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)

        weatherTextView = findViewById(R.id.weatherTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)
        humidityTextView = findViewById(R.id.humidityTextView)
        pressureTextView = findViewById(R.id.pressureTextView)
        windTextView = findViewById(R.id.windTextView)
        sunriseTextView = findViewById(R.id.sunriseTextView)
        sunsetTextView = findViewById(R.id.sunsetTextView)


        weatherIconImageView.visibility = ImageView.GONE

        searchButton.setOnClickListener {
            val city = cityEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeather(city)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeather(cityInput: String) {
        val city = URLEncoder.encode(cityInput.trim(), "UTF-8")
        val cityUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=$unit"

        // Helper functions for formatting
        fun formatTime(unixTime: Long): String {
            val date = java.util.Date(unixTime * 1000)
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getDefault()
            return sdf.format(date)
        }

        fun windDirection(deg: Int): String {
            val directions = listOf("N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW")
            val index = ((deg / 22.5) + 0.5).toInt() % 16
            return directions[index]
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(cityUrl).readText()
                val jsonObject = JSONObject(response)

                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val description = weatherObject.getString("description")
                val iconCode = weatherObject.getString("icon")  // e.g. "04d"

                val mainObject = jsonObject.getJSONObject("main")
                val temp = mainObject.getDouble("temp")
                val humidity = mainObject.getInt("humidity")
                val pressure = mainObject.getInt("pressure")

                val windObject = jsonObject.getJSONObject("wind")
                val windSpeed = windObject.getDouble("speed")
                val windDeg = windObject.optInt("deg", -1)
                val windDirStr = if (windDeg == -1) "N/A" else windDirection(windDeg)

                val sysObject = jsonObject.getJSONObject("sys")
                val sunriseUnix = sysObject.getLong("sunrise")
                val sunsetUnix = sysObject.getLong("sunset")
                val sunriseTime = formatTime(sunriseUnix)
                val sunsetTime = formatTime(sunsetUnix)

                val cityName = jsonObject.getString("name")

                val result = "City: $cityName\nTemperature: $temp°C\nCondition: $description"
                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}.png"

                withContext(Dispatchers.Main) {
                    weatherTextView.text = result

                    // Update extra details
                    humidityTextView.text = "Humidity: $humidity%"
                    pressureTextView.text = "Pressure: $pressure hPa"
                    windTextView.text = "Wind: $windSpeed m/s $windDirStr"
                    sunriseTextView.text = "Sunrise: $sunriseTime"
                    sunsetTextView.text = "Sunset: $sunsetTime"

                    // Show icon and set size
                    weatherIconImageView.visibility = ImageView.VISIBLE
                    val scale = resources.displayMetrics.density
                    val newSize = (200 * scale).toInt()
                    val params = weatherIconImageView.layoutParams
                    params.width = newSize
                    params.height = newSize
                    weatherIconImageView.layoutParams = params
                    weatherIconImageView.requestLayout()

                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(weatherIconImageView)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    weatherTextView.text = "Error: ${e.localizedMessage}"
                    weatherIconImageView.visibility = ImageView.GONE

                    // Clear extra info on error
                    humidityTextView.text = ""
                    pressureTextView.text = ""
                    windTextView.text = ""
                    sunriseTextView.text = ""
                    sunsetTextView.text = ""
                }
            }
        }
    }


}
