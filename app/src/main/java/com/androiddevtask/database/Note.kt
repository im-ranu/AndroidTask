package com.androiddevtask.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "UsersNotes")
data class Note (
        @PrimaryKey
        var username : String,
        var note : String )

