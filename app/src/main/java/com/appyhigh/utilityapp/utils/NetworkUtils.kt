package com.appyhigh.utilityapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {
    fun checkConnection(context: Context): Boolean {
        var hasInternet = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val networkInfo =
                    connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
            val networks: Array<Network> = connectivityManager.allNetworks
            if (networks.isNotEmpty()) {
                for (network in networks) {
                    val nc =
                        connectivityManager.getNetworkCapabilities(network)
                    if (nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) hasInternet =
                        true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return hasInternet
    }
}