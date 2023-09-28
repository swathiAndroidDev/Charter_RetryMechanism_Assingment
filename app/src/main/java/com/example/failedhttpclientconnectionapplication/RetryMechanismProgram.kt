package com.example.failedhttpclientconnectionapplication

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
import java.util.concurrent.TimeUnit

class HttpClient {
    fun postToServer(message: String): Boolean {
        val serverUrl = "https://example.com/api/postData" //sample url

        try {
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection

            // Set HTTP method to POST
            connection.requestMethod = "POST"

            // Set request headers (if needed)
            connection.setRequestProperty("Content-Type", "application/json")

            // Enable input/output streams for POST data
            connection.doOutput = true

            // Write the message (JSON) to the output stream
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(message.toByteArray(Charsets.UTF_8))
            outputStream.close()

            // Get the HTTP response code
            val responseCode = connection.responseCode

            // Check if the response code indicates success (e.g., HTTP 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // return true and no retry is required
                return true
            } else {
                // return false if failed and do retry
                return false
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues, server errors)
            // return false if failed and do retry
            e.printStackTrace()
            return false
        }
    }
}

private val random = Random()
/**
 * The exponentialBackoffWithRetry function attempts to post a
 * message to the server using exponential backoff with random value.
 * If the HTTP request fails, it waits for a random amount of time and then
 * retries with an exponentially increasing delay.
 */
fun exponentialBackoffWithRetry(httpClient: HttpClient, message: String) {
    val maxRetries = 5 //sample number of retries
    var retries = 0
    var delayMillis = 1_000L // Initial delay, in milliseconds

    while (retries < maxRetries) {
        try {
            if (httpClient.postToServer(message)) {
                // Successful post, exit retry loop
                break
            }
        } catch (e: Exception) {
            // Connection error, retry with exponential backoff
            val randomValue = random.nextInt(5_000) // Random between 0 and 4_999 ms
            val totalDelayMillis = delayMillis + randomValue.toLong()

            // Sleep with delay
            TimeUnit.MILLISECONDS.sleep(totalDelayMillis)

            // Double the delay for the next retry (exponential backoff)
            delayMillis *= 2
            retries++
        }
    }
}

fun main() {
    val httpClient = HttpClient()
    val message = "{\"data\": \"data to be post\"}"

    exponentialBackoffWithRetry(httpClient, message)
}
