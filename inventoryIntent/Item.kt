package com.bignerdranch.android.inventoryIntent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Item(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var model: String = "",
    var date: Date = Date(),
    var inShipment: Boolean = false,
    var supplier: String = "",
    var itemAmount: String = "",
    var time: String = ""

)