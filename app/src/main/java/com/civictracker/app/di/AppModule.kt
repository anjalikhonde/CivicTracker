package com.civictracker.app.di

import android.content.Context
import androidx.room.Room
import com.civictracker.app.data.local.AppDatabase
import com.civictracker.app.data.local.IssueDao
import com.civictracker.app.data.remote.CivicApi
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
    fun provideCivicApi(): CivicApi {
        return Retrofit.Builder()
            .baseUrl(CivicApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CivicApi::class.java)
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
}
