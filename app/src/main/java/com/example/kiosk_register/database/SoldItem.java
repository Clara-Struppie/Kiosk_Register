package com.example.kiosk_register.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "soldItems")
public class SoldItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int itemID;
    public int saleID;
    public int qty;
    public double priceAtSale;
}
