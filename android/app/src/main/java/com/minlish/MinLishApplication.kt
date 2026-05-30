package com.minlish

import android.app.Application
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.datastore.TokenManager
import com.minlish.core.network.AuthApiService
import com.minlish.core.network.AuthInterceptor
import com.minlish.core.network.DecksApiService
import com.minlish.core.network.FavoritesApiService
import com.minlish.core.network.ImportsApiService
import com.minlish.core.network.TokenAuthenticator
import com.minlish.core.network.UserApiService
import com.minlish.core.network.VocabulariesApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MinLishApplication : Application() {
    
    val tokenManager by lazy { TokenManager(this) }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val decksApiService: DecksApiService by lazy { retrofit.create(DecksApiService::class.java) }
    val vocabulariesApiService: VocabulariesApiService by lazy {
        retrofit.create(VocabulariesApiService::class.java)
    }
    val favoritesApiService: FavoritesApiService by lazy {
        retrofit.create(FavoritesApiService::class.java)
    }
    val importsApiService: ImportsApiService by lazy { retrofit.create(ImportsApiService::class.java) }

    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenManager) { authApiService })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val vocabularyRepository by lazy {
        VocabularyRepository(
            context = applicationContext,
            decksApi = decksApiService,
            vocabulariesApi = vocabulariesApiService,
            favoritesApi = favoritesApiService,
            importsApi = importsApiService,
        )
    }
    val settingsRepository by lazy { SettingsRepository(tokenManager) }
    val authRepository by lazy { AuthRepository(authApiService, tokenManager) }
    val userRepository by lazy { UserRepository(userApiService, tokenManager) }
}
