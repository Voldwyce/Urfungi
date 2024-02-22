package com.example.urfungi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.format.TextStyle

suspend fun fetchWeather(city: String, apiKey: String): WeatherData {
    val client = HttpClient()
    val url = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"
    val response: String = client.get(url)
    client.close()
    println("Response API: $response")
    return parseWeatherData(response)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp() {
    var city by remember { mutableStateOf("") }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    colors = TextFieldDefaults.textFieldColors(
                        cursorColor = Color.Gray,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp)
                )
                IconButton(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val data = fetchWeather(city, "38c686be37de60e6ef934c32434ed3ac")
                        withContext(Dispatchers.Main) {
                            weatherData = data
                        }
                    }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar")
                }
            }
            weatherData?.let {
                WeatherScreen(it)
            }
        }
    }
}

@Composable
fun WeatherScreen(weatherData: WeatherData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "🌡️ Temperatura: ${"%.1f".format(weatherData.temperature)}°C")
        Text(text = "🌡️ Temperatura mínima: ${"%.1f".format(weatherData.minTemperature)}°C")
        Text(text = "🌡️ Temperatura máxima: ${"%.1f".format(weatherData.maxTemperature)}°C")
        when {
            weatherData.temperature < 10 -> Text(text = "❄️ Hace frío")
            weatherData.temperature in 10.0..25.0 -> Text(text = "☀️ Temperatura normal")
            else -> Text(text = "🔥 Hace calor")
        }
        Spacer (modifier = Modifier.height(16.dp))
        Text(text = "💧 Humedad: ${weatherData.humidity}%")
        when {
            weatherData.humidity < 30 -> Text(text = "🏜️ Poca humedad")
            weatherData.humidity in 30.0..70.0 -> Text(text = "🌳 Humedad normal")
            else -> Text(text = "🌊 Mucha humedad")
        }
        Spacer (modifier = Modifier.height(16.dp))

        Text(text = "☔ Precipitación: ${weatherData.precipitation} mm")
        when {
            weatherData.precipitation < 1 -> Text(text = "🌤️ Poca precipitación")
            weatherData.precipitation in 1.0..10.0 -> Text(text = "🌦️ Precipitación normal")
            else -> Text(text = "🌧️ Mucha precipitación")
        }
        Spacer (modifier = Modifier.height(16.dp))
        Text(text = "💨 Velocidad del viento: ${weatherData.windSpeed} km/h")
        when {
            weatherData.windSpeed < 10 -> Text(text = "🍃 Viento suave")
            weatherData.windSpeed in 10.0..30.0 -> Text(text = "🌬️ Viento normal")
            else -> Text(text = "🌪️ Viento fuerte")
        }
    }
}

fun parseWeatherData(response: String): WeatherData {
    val json = Json.parseToJsonElement(response).jsonObject
    val main = json["main"]!!.jsonObject
    val wind = json["wind"]!!.jsonObject
    val rain = json["rain"]?.jsonObject

    return WeatherData(
        temperature = main["temp"]!!.jsonPrimitive.double - 273.15,
        minTemperature = main["temp_min"]!!.jsonPrimitive.double - 273.15,
        maxTemperature = main["temp_max"]!!.jsonPrimitive.double - 273.15,
        humidity = main["humidity"]!!.jsonPrimitive.double,
        windSpeed = wind["speed"]!!.jsonPrimitive.double,
        precipitation = rain?.get("1h")?.jsonPrimitive?.double ?: 0.0
    )
}