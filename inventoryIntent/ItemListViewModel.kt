package com.bignerdranch.android.inventoryIntent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ItemListViewModel : ViewModel() {
    private val itemRepository = ItemRepository.get()
    val itemListLiveData = itemRepository.getItems()

    fun addItem(item: Item) {
        itemRepository.addItem(item)
    }
    //app run
    fun searchItemsByTitle(searchQuery: String): LiveData<List<Item>> {
        return itemRepository.searchItemsByTitle(searchQuery)
    }
    //test
    fun getItems(): LiveData<List<Item>> {
        return itemListLiveData
    }}
