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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cityEditText = findViewById<EditText>(R.id.cityEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)
        weatherTextView = findViewById(R.id.weatherTextView)
        weatherIconImageView = findViewById(R.id.weatherIconImageView)

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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(cityUrl).readText()
                val jsonObject = JSONObject(response)
                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherObject = weatherArray.getJSONObject(0)
                val description = weatherObject.getString("description")
                val iconCode = weatherObject.getString("icon")  // e.g. "04d"

                val temp = jsonObject.getJSONObject("main").getDouble("temp")
                val cityName = jsonObject.getString("name")

                val result = "City: $cityName\nTemperature: $temp°C\nCondition: $description"
                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}.png"

                // Log the icon URL for debugging
                Log.d("WeatherIconDebug", "Icon URL: $iconUrl")

                withContext(Dispatchers.Main) {
                    weatherTextView.text = result

                    weatherIconImageView.visibility = ImageView.VISIBLE

                    // Use Glide with placeholders and error images
                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal) // system default loading
                        .error(android.R.drawable.stat_notify_error) // system error icon
                        .into(weatherIconImageView)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    weatherTextView.text = "Error: ${e.localizedMessage}"
                    weatherIconImageView.visibility = ImageView.GONE
                }
            }
        }
    }
}
