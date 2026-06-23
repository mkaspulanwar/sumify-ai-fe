package id.antasari.sumifyai.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val PREFS_NAME = "sumifyai_prefs"
    private const val KEY_BASE_URL = "api_base_url"
    
    // Default to Android Emulator host localhost mapping
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private var activeService: SumifyApiService? = null
    private var activeUrl: String? = null

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        // Ensure URL ends with a slash
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }

    fun setBaseUrl(context: Context, newUrl: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var formattedUrl = newUrl.trim()
        if (formattedUrl.isNotEmpty() && !formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
        // Reset cached instances
        activeService = null
        activeUrl = null
    }

    fun getApiService(context: Context): SumifyApiService {
        val url = getBaseUrl(context)
        if (activeService != null && activeUrl == url) {
            return activeService!!
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        activeUrl = url
        activeService = retrofit.create(SumifyApiService::class.java)
        return activeService!!
    }
}
