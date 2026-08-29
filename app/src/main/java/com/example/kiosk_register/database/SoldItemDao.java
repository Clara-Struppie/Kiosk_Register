package com.example.kiosk_register.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.HashMap;
import java.util.List;

@Dao
public interface SoldItemDao {
    @Insert
    void insert(SoldItem soldItem);

    @Query("SELECT * FROM soldItems ORDER BY id ASC")
    List<SoldItem> getAllSoldItems();

    @Query("SELECT items.name, soldItems.quantity FROM soldItems JOIN items ON soldItems.itemID=items.id WHERE soldItems.saleID IN (:saleIDList)")
    List<SoldItemWithName> getSoldItemsFromSalesList(List<Integer> saleIDList);
}
