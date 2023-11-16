package com.bignerdranch.android.inventoryIntent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.inventoryIntent.Item
import java.util.*

@Dao
interface ItemDao {
    @Query("SELECT * FROM Item")
    fun getItems(): LiveData<List<Item>>

    @Query("SELECT * FROM Item WHERE id=(:id)")
    fun getItem(id: UUID): LiveData<Item?>

    @Update
    fun updateItem(item: Item)

    @Insert
    fun addItem(item: Item)


    @Query("SELECT * FROM Item WHERE model LIKE '%' || :searchQuery || '%'")
    fun searchItemsByTitle(searchQuery: String): LiveData<List<Item>>


}