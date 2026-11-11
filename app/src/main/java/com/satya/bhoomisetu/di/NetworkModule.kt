package com.satya.bhoomisetu.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.satya.bhoomisetu.data.remote.AuthApiService
import com.satya.bhoomisetu.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenAuthenticator: com.satya.bhoomisetu.data.remote.TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSurveyRequestApiService(retrofit: Retrofit): com.satya.bhoomisetu.data.remote.SurveyRequestApiService {
        return retrofit.create(com.satya.bhoomisetu.data.remote.SurveyRequestApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLegalAdvisorApiService(retrofit: Retrofit): com.satya.bhoomisetu.data.remote.LegalAdvisorApiService {
        return retrofit.create(com.satya.bhoomisetu.data.remote.LegalAdvisorApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDocumentWriterApiService(retrofit: Retrofit): com.satya.bhoomisetu.data.remote.DocumentWriterApiService {
        return retrofit.create(com.satya.bhoomisetu.data.remote.DocumentWriterApiService::class.java)
    }
}

