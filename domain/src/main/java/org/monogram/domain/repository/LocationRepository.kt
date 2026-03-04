package org.monogram.domain.repository

import org.monogram.domain.models.webapp.OSMReverseResponse

interface LocationRepository {
    suspend fun reverseGeocode(lat: Double, lon: Double): OSMReverseResponse?
    suspend fun searchLocation(query: String): List<OSMReverseResponse>
}