package com.example.kiosk_register.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales")
public class Sale {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;

    public double total;
}
