package com.civictracker.app.di

import com.civictracker.app.data.repository.SupabaseRepository
import android.content.Context
import androidx.room.Room
import com.civictracker.app.data.local.AppDatabase
import com.civictracker.app.data.local.IssueDao
import com.civictracker.app.data.remote.CivicApi
import com.civictracker.app.data.remote.MLApiService
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideCivicApi(): CivicApi {
        return Retrofit.Builder()
            .baseUrl(CivicApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CivicApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMLApiService(): MLApiService {
        return Retrofit.Builder()
            .baseUrl(MLApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MLApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "civic_tracker_db"
        ).build()
    }

    @Provides
    fun provideIssueDao(database: AppDatabase): IssueDao {
        return database.issueDao()
    }

    @Provides
    @Singleton
    fun provideSupabaseRepository(): SupabaseRepository {
        return SupabaseRepository()
    }
}
