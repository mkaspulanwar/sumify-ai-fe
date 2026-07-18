package id.antasari.sumifyai.data.api

import id.antasari.sumifyai.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private val baseUrl: String by lazy {
        val configuredUrl = BuildConfig.API_BASE_URL.trim()
        require(configuredUrl.isNotEmpty()) {
            "API base URL is not configured for this build."
        }

        val normalizedUrl = if (configuredUrl.endsWith("/")) {
            configuredUrl
        } else {
            "$configuredUrl/"
        }

        val parsedUrl = normalizedUrl.toHttpUrlOrNull()
        require(parsedUrl != null) {
            "API base URL is invalid: $normalizedUrl"
        }
        require(BuildConfig.DEBUG || parsedUrl.isHttps) {
            "Release API base URL must use HTTPS."
        }
        normalizedUrl
    }

    private val serviceInstance: SumifyApiService by lazy {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
            .create(SumifyApiService::class.java)
    }

    fun getApiService(): SumifyApiService = serviceInstance
}
