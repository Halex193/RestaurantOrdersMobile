package com.gradysbooch.restaurant.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//TODO Enable Full Text Search (FTS4) https://developer.android.com/training/data-storage/room/defining-data#fts
@Entity
data class MenuItem(@PrimaryKey val menuItemId: Int, val name: String, val price: Int)