package com.minlish.core.network

import com.minlish.core.datastore.TokenManager
import com.minlish.core.network.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiServiceLazy: () -> AuthApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) {
            return null
        }

        if (response.request.url.encodedPath.contains("auth/refresh")) {
            return null
        }

        synchronized(this) {
            val currentToken = runBlocking { tokenManager.getAccessTokenBlocking() }
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            if (currentToken != requestToken && !currentToken.isNullOrEmpty()) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = runBlocking { tokenManager.getRefreshTokenBlocking() }
            if (refreshToken.isNullOrEmpty()) {
                handleLogout()
                return null
            }

            val authService = authApiServiceLazy()
            return try {
                val refreshResponse = authService.refreshSync(RefreshTokenRequest(refreshToken)).execute()
                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newAuth = refreshResponse.body()!!
                    runBlocking {
                        tokenManager.saveTokens(newAuth.accessToken, newAuth.refreshToken)
                    }
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newAuth.accessToken}")
                        .build()
                } else {
                    handleLogout()
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun handleLogout() {
        runBlocking {
            tokenManager.clearAuth()
        }
    }
}
