package com.androiddevtask.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface NoteDao {
    @Insert
    fun insertNote(note : Note)

    @Query("SELECT * FROM UsersNotes WHERE username == :username")
    fun getNoteByUser(username: String): Note
}