package org.monogram.data.repository

import android.util.Log
import org.monogram.domain.models.webapp.OSMReverseResponse
import org.monogram.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LocationRepositoryImpl : LocationRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val userAgent = "MonoGram-Android-App/1.0"

    override suspend fun reverseGeocode(lat: Double, lon: Double): OSMReverseResponse? {
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$lat&lon=$lon&addressdetails=1"

        return try {
            val responseText = makeHttpRequest(url)
            if (responseText != null) {
                json.decodeFromString<OSMReverseResponse>(responseText)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "Error parsing reverse geocode", e)
            null
        }
    }

    override suspend fun searchLocation(query: String): List<OSMReverseResponse> {
        return try {
            val encodedQuery = withContext(Dispatchers.IO) {
                URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            }
            val url =
                "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=jsonv2&addressdetails=1&limit=10"

            val responseText = makeHttpRequest(url)
            if (responseText != null) {
                json.decodeFromString<List<OSMReverseResponse>>(responseText)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "Error parsing search results", e)
            emptyList()
        }
    }

    private suspend fun makeHttpRequest(urlString: String): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                Log.e("LocationRepo", "Nominatim Error: $responseCode for URL: $urlString")
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepo", "Network error", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}