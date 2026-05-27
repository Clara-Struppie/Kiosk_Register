package com.example.kiosk_register.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SoldItemDao {
    @Insert
    void insert(SoldItem soldItem);

    @Query("SELECT * FROM soldItems ORDER BY id ASC")
    List<SoldItem> getAllSoldItems();

    //TO DO:
    //Query for statistics
}
