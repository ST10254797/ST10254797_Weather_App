package com.example.weatherapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val apiKey = "c2cfb03bac68bbf380f03cdbc32a83e4"
    private val unit = "metric"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cityEditText = findViewById<EditText>(R.id.cityEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val weatherTextView = findViewById<TextView>(R.id.weatherTextView)

        searchButton.setOnClickListener {
            val city = cityEditText.text.toString().trim()
            if (city.isNotEmpty()) {
                getWeather(city, weatherTextView)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeather(city: String, weatherTextView: TextView) {
        val cityUrl = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=$unit"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(cityUrl).readText()
                val jsonObject = JSONObject(response)
                val weatherArray = jsonObject.getJSONArray("weather")
                val description = weatherArray.getJSONObject(0).getString("description")
                val temp = jsonObject.getJSONObject("main").getDouble("temp")
                val cityName = jsonObject.getString("name")

                val result = "City: $cityName\nTemperature: $temp°C\nCondition: $description"

                withContext(Dispatchers.Main) {
                    weatherTextView.text = result
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    weatherTextView.text = "City not found or error occurred"
                }
            }
        }
    }
}
