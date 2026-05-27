package com.example.kiosk_register.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales")
public class Sale {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "timeOfSale")
    public long timestamp;
    @ColumnInfo(name = "totalPrice")
    public double total;
}
