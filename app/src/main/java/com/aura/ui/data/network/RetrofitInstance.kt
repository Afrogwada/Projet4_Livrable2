package com.aura.ui.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
/**
 * Singleton pour accéder à l'API via Retrofit.
 */
object RetrofitInstance {

    // ⚠️ Pour émulateur Android Studio : 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val loginApi: LoginApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LoginApiService::class.java)
    }
    val getAccounts: GetAccountsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GetAccountsApiService::class.java)
    }
    val transferApi: TransferApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TransferApiService::class.java)
    }
}