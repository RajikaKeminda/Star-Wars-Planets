package com.example.starwarsplanets.di

import android.content.Context
import com.example.starwarsplanets.data.api.SwapiService
import com.example.starwarsplanets.data.local.PlanetDao
import com.example.starwarsplanets.data.local.PlanetDatabase
import com.example.starwarsplanets.data.repository.PlanetRepository
import com.example.starwarsplanets.util.NetworkConnectivityUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideSwapiService(okHttpClient: OkHttpClient): SwapiService {
        return Retrofit.Builder()
            .baseUrl("https://swapi.dev/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwapiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlanetDatabase(@ApplicationContext context: Context): PlanetDatabase {
        return PlanetDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providePlanetDao(database: PlanetDatabase): PlanetDao {
        return database.planetDao()
    }

    @Provides
    @Singleton
    fun providePlanetRepository(
        swapiService: SwapiService,
        planetDao: PlanetDao
    ): PlanetRepository {
        return PlanetRepository(swapiService, planetDao)
    }


    @Provides
    @Singleton
    fun provideNetworkConnectivityUtil(@ApplicationContext context: Context): NetworkConnectivityUtil {
        return NetworkConnectivityUtil(context)
    }
}