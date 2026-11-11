package com.satya.bhoomisetu.di

import com.satya.bhoomisetu.data.repository.AuthRepositoryImpl
import com.satya.bhoomisetu.data.repository.PropertyRepositoryImpl
import com.satya.bhoomisetu.data.repository.SurveyRequestRepositoryImpl
import com.satya.bhoomisetu.domain.repository.AuthRepository
import com.satya.bhoomisetu.domain.repository.PropertyRepository
import com.satya.bhoomisetu.domain.repository.SurveyRequestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPropertyRepository(
        impl: PropertyRepositoryImpl
    ): PropertyRepository

    @Binds
    @Singleton
    abstract fun bindSurveyRequestRepository(
        impl: SurveyRequestRepositoryImpl
    ): SurveyRequestRepository

    @Binds
    @Singleton
    abstract fun bindLegalAdvisorRepository(
        impl: com.satya.bhoomisetu.data.repository.LegalAdvisorRepositoryImpl
    ): com.satya.bhoomisetu.domain.repository.LegalAdvisorRepository

    @Binds
    @Singleton
    abstract fun bindDocumentWriterRepository(
        impl: com.satya.bhoomisetu.data.repository.DocumentWriterRepositoryImpl
    ): com.satya.bhoomisetu.domain.repository.DocumentWriterRepository
}

