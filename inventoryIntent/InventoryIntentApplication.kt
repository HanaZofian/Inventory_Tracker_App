package com.bignerdranch.android.inventoryIntent

import android.app.Application

class InventoryIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ItemRepository.initialize(this)
    }
}