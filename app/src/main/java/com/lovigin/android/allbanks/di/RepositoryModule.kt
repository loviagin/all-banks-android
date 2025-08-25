package com.lovigin.android.allbanks.di

import com.lovigin.android.allbanks.data.repo.RatesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun ratesRepository(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): RatesRepository = RatesRepository(context, client)
}