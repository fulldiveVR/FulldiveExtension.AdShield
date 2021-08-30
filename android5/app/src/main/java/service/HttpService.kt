package service

import engine.EngineService
import model.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

object HttpService {

    private val engine = EngineService
    private val env = EnvironmentService

    private val httpClient = OkHttpClient.Builder().apply {
        addNetworkInterceptor { chain ->
            val request = chain.request()
            chain.connection()?.socket()?.let {
                engine.protectSocket(it)
            }
            chain.proceed(request)
        }
        addInterceptor { chain ->
            val request = chain.request().newBuilder().header("User-Agent", env.getUserAgent()).build()
            chain.proceed(request)
        }

        if (!env.isPublicBuild()) addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()

    fun getClient() = httpClient

    fun makeRequest(url: Uri): String {
        val request = Request.Builder()
            .url(url)
            .build()
        return httpClient.newCall(request).execute().body!!.string()
    }

}