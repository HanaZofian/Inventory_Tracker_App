package com.bignerdranch.android.inventoryIntent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.inventoryIntent.Item

@Database(entities = [Item::class], version = 4, exportSchema = false)
@TypeConverters(ItemTypeConverters::class)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun ItemDao(): ItemDao
}
const val oldTableName = "Crime"
const val newTableName = "Item"

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $oldTableName RENAME TO $newTableName")
        database.execSQL(
            "ALTER TABLE $newTableName ADD COLUMN supplier TEXT NOT NULL DEFAULT ''"
        )
    }
}
val migration_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $oldTableName RENAME TO $newTableName")
        database.execSQL(
            "ALTER TABLE $newTableName ADD COLUMN itemAmount TEXT NOT NULL DEFAULT ''"
        )
    }
}

val migration_3_4 = object : Migration(3,4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $oldTableName RENAME TO $newTableName")
        database.execSQL(
            "ALTER TABLE $newTableName ADD COLUMN time TEXT NOT NULL DEFAULT ''"
        )
    }
}