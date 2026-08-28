package com.example.kiosk_register.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SaleDao {

    @Insert
    void insert(Sale sale);

    @Query("SELECT * FROM sales WHERE timeOfSale >= (:currentTime - 2592000000) ORDER BY timeOfSale ASC")
    List<Sale> getLatestSales(long currentTime);
    @Query("SELECT id FROM sales WHERE timeOfSale = :timestamp")
    int getRecentSaleID(long timestamp);
}
