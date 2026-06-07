package com.example.kiosk_register.database;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    @NonNull
    private String name;
    @ColumnInfo(name = "price")
    private double price;
    @ColumnInfo(name = "active")
    private boolean active;
    @ColumnInfo(name = "buttonNumber")
    private int buttonNumber;

    public Item() {
        this.name = "";
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public int getButtonNumber() {
        return buttonNumber;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setButtonNumber(int buttonNumber) {
        this.buttonNumber = buttonNumber;
    }

    public void setId(int id) {
        this.id = id;
    }
}