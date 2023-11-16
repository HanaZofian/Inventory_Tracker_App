package com.bignerdranch.android.inventoryIntent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class ItemDetailViewModel : ViewModel() {
    private val itemRepository: ItemRepository = ItemRepository.get()
    private val itemIdLiveData = MutableLiveData<UUID>()


    var itemLiveData: LiveData<Item?> = Transformations.switchMap(itemIdLiveData) {
        itemRepository.getItem(it)
    }

    fun loadItem(itemId: UUID) {
        itemIdLiveData.value = itemId
    }

    fun saveItem(item: Item) {
        itemRepository.updateItem(item)
    }


}