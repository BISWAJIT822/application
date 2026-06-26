package com.goatinsurance.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.location.Location
import android.location.LocationManager
import android.os.Environment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Constants for the application.
 */
object Constants {
    const val PREFS_NAME = "goat_insurance_prefs"
    const val SYNC_WORK_NAME = "goat_insurance_sync_work"
}

/**
 * Formats dates for the system.
 */
object DateUtils {
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val date = apiDateFormat.parse(dateStr) ?: return dateStr
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatToDateOnly(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val date = apiDateFormat.parse(dateStr) ?: return dateStr
            dateOnlyFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getTodayString(): String {
        return apiDateFormat.format(Date())
    }
}

/**
 * Connectivity observer for offline-first support.
 */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Initial status
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        trySend(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

/**
 * Captures GPS locations for enrollment and claims.
 */
object LocationUtils {
    fun getLastKnownLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            gpsLocation ?: networkLocation
        } catch (e: SecurityException) {
            null
        }
    }
}

/**
 * File helpers for photo uploads and local storage.
 */
object FileUtils {
    fun createImageFile(context: Context, fileName: String): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "${fileName}_${System.currentTimeMillis()}.jpg")
    }

    fun clearTempFiles(context: Context) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        storageDir?.listFiles()?.forEach { file ->
            if (file.name.contains("temp_") || System.currentTimeMillis() - file.lastModified() > 86400000) {
                file.delete()
            }
        }
    }
}
