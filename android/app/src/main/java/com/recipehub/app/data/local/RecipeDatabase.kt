package com.recipehub.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS collections (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS recipe_collection_cross_ref (recipeLocalId INTEGER NOT NULL, collectionId INTEGER NOT NULL, PRIMARY KEY(recipeLocalId, collectionId))"
        )
    }
}

@Database(
    entities = [RecipeEntity::class, CollectionEntity::class, RecipeCollectionCrossRef::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun collectionDao(): CollectionDao
}
