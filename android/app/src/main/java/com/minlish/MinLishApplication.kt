package com.minlish

import android.app.Application
import com.minlish.core.audio.TextToSpeechManager
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
import com.minlish.core.network.PracticeApiService
import com.minlish.core.network.LearningApiService
import com.minlish.core.network.TokenAuthenticator
import com.minlish.core.network.UserApiService
import com.minlish.core.network.VocabulariesApiService
import com.minlish.core.network.LevelsApiService
import com.minlish.core.data.repository.AnalyticsRepository
import com.minlish.core.data.repository.NotificationRepository
import com.minlish.feature.analytics.data.AnalyticsApiService
import com.minlish.feature.settings.data.NotificationApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.getValue

class MinLishApplication : Application() {
    
    val tokenManager: TokenManager by lazy { TokenManager(this) }
    val textToSpeechManager by lazy { TextToSpeechManager(this) }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val levelsApiService: LevelsApiService by lazy { retrofit.create(LevelsApiService::class.java) }
    val decksApiService: DecksApiService by lazy { retrofit.create(DecksApiService::class.java) }
    val vocabulariesApiService: VocabulariesApiService by lazy {
        retrofit.create(VocabulariesApiService::class.java)
    }
    val favoritesApiService: FavoritesApiService by lazy {
        retrofit.create(FavoritesApiService::class.java)
    }
    val importsApiService: ImportsApiService by lazy { retrofit.create(ImportsApiService::class.java) }
    val practiceApiService: PracticeApiService by lazy { retrofit.create(PracticeApiService::class.java) }
    val learningApiService: LearningApiService by lazy { retrofit.create(LearningApiService::class.java) }

    //Khai báo ApiService (Dev E )
    val analyticsApiService: AnalyticsApiService by lazy { retrofit.create(AnalyticsApiService::class.java) }
    val notificationApiService: NotificationApiService by lazy { retrofit.create(NotificationApiService::class.java) }

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

    val vocabularyRepository by lazy {
        VocabularyRepository(
            context = applicationContext,
            decksApi = decksApiService,
            vocabulariesApi = vocabulariesApiService,
            favoritesApi = favoritesApiService,
            importsApi = importsApiService,
            practiceApi = practiceApiService,
            learningApi = learningApiService,
        )
    }
    val settingsRepository by lazy { SettingsRepository(tokenManager) }
    val authRepository by lazy { AuthRepository(authApiService, tokenManager) }
    val userRepository by lazy { UserRepository(userApiService, levelsApiService, tokenManager) }

    //Khai báo Repository (Dev E )
    val analyticsRepository by lazy { AnalyticsRepository(analyticsApiService) }
    val notificationRepository by lazy { NotificationRepository(notificationApiService) }
}
