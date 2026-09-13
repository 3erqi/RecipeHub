package com.recipehub.app.di

import android.content.Context
import androidx.room.Room
import com.recipehub.app.data.local.CollectionDao
import com.recipehub.app.data.local.MIGRATION_1_2
import com.recipehub.app.data.local.RecipeDao
import com.recipehub.app.data.local.RecipeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(@ApplicationContext context: Context): RecipeDatabase =
        Room.databaseBuilder(context, RecipeDatabase::class.java, "recipehub.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideRecipeDao(database: RecipeDatabase): RecipeDao = database.recipeDao()

    @Provides
    @Singleton
    fun provideCollectionDao(database: RecipeDatabase): CollectionDao = database.collectionDao()
}
