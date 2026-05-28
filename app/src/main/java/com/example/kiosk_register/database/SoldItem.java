package com.example.kiosk_register.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "soldItems",
        foreignKeys = {
                @ForeignKey(entity = Sale.class,
                        parentColumns = "id",
                        childColumns = "saleID",
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(entity = Item.class,
                        parentColumns = "id",
                        childColumns = "itemID",
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE
                )},
        indices = {@Index(value = {"id"})})
public class SoldItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "itemID")
    public int itemID;
    @ColumnInfo(name = "saleID")
    public int saleID;
    @ColumnInfo(name = "quantity")
    public int qty;
    @ColumnInfo(name = "priceAtSale")
    public double priceAtSale;
}
