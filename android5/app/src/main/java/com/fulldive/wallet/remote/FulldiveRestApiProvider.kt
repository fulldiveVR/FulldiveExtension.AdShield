package com.fulldive.wallet.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.adshield.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class FulldiveRestApiProvider {

    val gson: Gson = GsonBuilder()
        .create()

    val defaultClientBuilder = createDefaultOkHttpClientBuilder()

    fun createDefaultOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        readTimeout(30, TimeUnit.SECONDS)
        connectTimeout(30, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }
            })

            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }
            sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            hostnameVerifier { _, _ -> true }
        }
    }

    fun getRetrofitApi(
        baseUrl: String,
        builder: OkHttpClient.Builder = defaultClientBuilder
    ): FullDiveApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(builder.build())
        .build()
        .create(FullDiveApi::class.java)
}
