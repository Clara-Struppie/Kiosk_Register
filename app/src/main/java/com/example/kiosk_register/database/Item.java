package com.example.kiosk_register.database;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "name")
    @NonNull
    public String name;
    @ColumnInfo(name = "price")
    public double price;
    @ColumnInfo(name = "active")
    public boolean active;
    @ColumnInfo(name = "buttonNumber")
    public int buttonNumber;

    public Item() {
        name = "";
    }
}