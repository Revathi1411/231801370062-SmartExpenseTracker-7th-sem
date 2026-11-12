package com.example.expensetracker

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Gets the exchange rate (1 unit of currency -> INR) from the Frankfurter API
object CurrencyConverter {

    // callback gives (rate, errorMessage). One of them is always null.
    fun getRate(fromCurrency: String, callback: (Double?, String?) -> Unit) {
        val handler = Handler(Looper.getMainLooper())

        // Network work must run on a background thread
        Thread {
            try {
                val url = URL("https://api.frankfurter.app/latest?from=$fromCurrency&to=INR")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val code = connection.responseCode
                if (code == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val rate = JSONObject(response).getJSONObject("rates").getDouble("INR")
                    handler.post { callback(rate, null) }
                } else {
                    handler.post { callback(null, "Server error ($code). Try again later.") }
                }
                connection.disconnect()
            } catch (e: Exception) {
                handler.post { callback(null, "Could not get the exchange rate. Check your internet.") }
            }
        }.start()
    }
}
