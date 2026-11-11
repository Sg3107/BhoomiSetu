package com.satya.bhoomisetu.data.remote

import android.util.Log
import com.google.gson.Gson
import com.satya.bhoomisetu.data.remote.dto.AuthResponse
import com.satya.bhoomisetu.data.remote.dto.RefreshTokenRequest
import com.satya.bhoomisetu.util.Constants
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val gson: Gson
) : Authenticator {

    private val refreshMutex = Mutex()

    // Isolated client to execute refresh request without re-invoking the authenticator
    private val refreshHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if multiple 401s occur
        if (responseCount(response) >= 3) {
            Log.w("TokenAuthenticator", "Exceeded maximum retry count for 401 Unauthorized")
            return null
        }

        // Never attempt to re-authenticate auth endpoints (login, register, refresh)
        val path = response.request.url.encodedPath
        if (path.contains("auth/refresh") || path.contains("auth/login") || path.contains("auth/register")) {
            return null
        }

        return runBlocking {
            refreshMutex.withLock {
                val savedAccessToken = sessionManager.getAccessToken()
                val requestHeader = response.request.header("Authorization")
                val requestToken = requestHeader?.removePrefix("Bearer ")?.trim()

                // If another concurrent request already refreshed the token, simply retry with the fresh one
                if (!savedAccessToken.isNullOrBlank() && savedAccessToken != requestToken) {
                    Log.d("TokenAuthenticator", "Re-using fresh token refreshed by a concurrent request")
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $savedAccessToken")
                        .build()
                }

                val currentRefreshToken = sessionManager.getRefreshToken()
                if (currentRefreshToken.isNullOrBlank()) {
                    Log.w("TokenAuthenticator", "No refresh token available in session")
                    return@withLock null
                }

                Log.d("TokenAuthenticator", "Attempting silent token refresh with backend...")
                val newAuthResponse = performTokenRefresh(currentRefreshToken)
                if (newAuthResponse != null && newAuthResponse.accessToken.isNotBlank()) {
                    Log.d("TokenAuthenticator", "Token refresh successful, updating session")
                    sessionManager.updateTokens(
                        accessToken = newAuthResponse.accessToken,
                        refreshToken = newAuthResponse.refreshToken
                    )

                    // Retry original request with the brand new access token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newAuthResponse.accessToken}")
                        .build()
                } else {
                    Log.w("TokenAuthenticator", "Token refresh failed or refresh token expired")
                    null
                }
            }
        }
    }

    private fun performTokenRefresh(refreshToken: String): AuthResponse? {
        return try {
            val refreshUrl = "${Constants.BASE_URL}auth/refresh"
            val jsonPayload = gson.toJson(RefreshTokenRequest(refreshToken = refreshToken))
            val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val callResponse = refreshHttpClient.newCall(request).execute()
            if (callResponse.isSuccessful) {
                val bodyString = callResponse.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    gson.fromJson(bodyString, AuthResponse::class.java)
                } else null
            } else {
                Log.w("TokenAuthenticator", "Refresh endpoint returned HTTP ${callResponse.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Exception during token refresh: ${e.message}")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
