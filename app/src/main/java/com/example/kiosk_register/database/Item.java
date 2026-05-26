package com.example.kiosk_register.database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public double price;

    public boolean active;
}