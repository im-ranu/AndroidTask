package com.androiddevtask.database

import androidx.room.*


@Dao
interface NoteDao {
    @Insert
    fun insertNote(note : Note)

    @Query("SELECT * FROM UsersNotes WHERE username == :username")
    fun getNoteByUser(username: String): Note

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(note: Note)
}