package com.example.kiosk_register.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);

    // Used to "remove" an item from the list of items without interfering with the statistics
    @Query("UPDATE items SET active = 0 WHERE id = :itemID")
    void toggleOff(int itemID);
    @Query("UPDATE items SET active = 1 WHERE id = :itemID")
    void toggleOn(int itemID);
    @Query("SELECT * FROM items ORDER BY id ASC")
    List<Item> getAllItems();
}
