package com.example.kiosk_register.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SaleDao {

    @Insert
    void insert(Sale sale);

    @Query("SELECT * FROM sales ORDER BY id ASC")
    List<Sale> getAllSales();
    @Query("SELECT id FROM sales WHERE timeOfSale = :timestamp")
    int getRecentSaleID(long timestamp);

    //TO DO:
    //Query for statistics
}
