package com.minlish

import android.app.Application
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.datastore.TokenManager
import com.minlish.core.network.AuthApiService
import com.minlish.core.network.AuthInterceptor
import com.minlish.core.network.PracticeApiService
import com.minlish.core.network.TokenAuthenticator
import com.minlish.core.network.UserApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.getValue

class MinLishApplication : Application() {
    
    val tokenManager: TokenManager by lazy { TokenManager(this) }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val practiceApiService: PracticeApiService by lazy { retrofit.create(PracticeApiService::class.java) }

    private val okHttpClient: OkHttpClient by lazy {
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

    private val retrofit: Retrofit by lazy {
        val baseUrl = BuildConfig.API_BASE_URL
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    val vocabularyRepository: VocabularyRepository by lazy { VocabularyRepository(practiceApiService) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(tokenManager) }
    val authRepository: AuthRepository by lazy { AuthRepository(authApiService, tokenManager) }
    val userRepository: UserRepository by lazy { UserRepository(userApiService, tokenManager) }
}
