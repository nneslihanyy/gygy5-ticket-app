package com.turkcell.data.di

import com.turkcell.core.domain.AuthRepository
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.repository.AuthRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val dataModule = module {

    // JSON parser — bilinmeyen alanları yoksay
    single {
        Json { ignoreUnknownKeys = true }
    }

    // OkHttp istemcisi + loglama
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    // Retrofit instance
    single {
        Retrofit.Builder()
            .baseUrl("https://tickets-api.halitkalayci.com/")
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // AuthApi — Retrofit servis arayüzü
    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }

    // AuthRepository — interface'e bağla
    single<AuthRepository> {
        AuthRepositoryImpl(authApi = get())
    }
}
