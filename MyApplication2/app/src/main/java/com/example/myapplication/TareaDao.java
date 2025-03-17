package com.example.myapplication;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TareaDao {

    @Query("SELECT * FROM tareas ORDER BY id DESC")
    LiveData<List<Tarea>> getAllTareas();

    @Insert
    void insertTarea(Tarea tarea);

    @Update
    void updateTarea(Tarea tarea);

    @Delete
    void deleteTarea(Tarea tarea);
}
