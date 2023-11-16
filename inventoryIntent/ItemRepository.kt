package com.bignerdranch.android.inventoryIntent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.inventoryIntent.database.ItemDao
import com.bignerdranch.android.inventoryIntent.database.ItemDatabase
import com.bignerdranch.android.inventoryIntent.database.migration_1_2
import com.bignerdranch.android.inventoryIntent.database.migration_2_3
import com.bignerdranch.android.inventoryIntent.database.migration_3_4
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class ItemRepository private constructor(context: Context) {
    private val database: ItemDatabase =
        Room.databaseBuilder(context.applicationContext, ItemDatabase::class.java, DATABASE_NAME)
            .addMigrations(migration_1_2)
            .addMigrations(migration_2_3)
            .addMigrations(migration_3_4)
            .build()
    private val itemDao: ItemDao = database.ItemDao()

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getItems(): LiveData<List<Item>> = itemDao.getItems()

    fun getItem(id: UUID): LiveData<Item?> = itemDao.getItem(id)

    fun updateItem(item: Item) {
        executor.execute {
            itemDao.updateItem(item)
        }
    }

    fun addItem(item: Item) {
        executor.execute {
            itemDao.addItem(item)
        }
    }


    // New method to get crimes based on the search query
    fun searchItemsByTitle(searchQuery: String): LiveData<List<Item>> {
        return itemDao.searchItemsByTitle(searchQuery)
    }

    companion object {
        private var INSTANCE: ItemRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ItemRepository(context)
            }
        }

        fun get(): ItemRepository {
            return INSTANCE ?: throw IllegalStateException("ItemRepository must be initialized")
        }
    }

}
