package com.example.kiosk_register.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales")
public class Sale {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "timeOfSale")
    private long timestamp;
    @ColumnInfo(name = "totalPrice")
    private double total;

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getTotal() {
        return total;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
