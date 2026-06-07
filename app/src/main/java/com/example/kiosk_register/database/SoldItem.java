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
    private int id;
    @ColumnInfo(name = "itemID")
    private int itemID;
    @ColumnInfo(name = "saleID")
    private int saleID;
    @ColumnInfo(name = "quantity")
    private int qty;
    @ColumnInfo(name = "priceAtSale")
    private double priceAtSale;

    public int getId() {
        return id;
    }

    public int getItemID() {
        return itemID;
    }

    public int getSaleID() {
        return saleID;
    }

    public int getQty() {
        return qty;
    }

    public double getPriceAtSale() {
        return priceAtSale;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public void setSaleID(int saleID) {
        this.saleID = saleID;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setPriceAtSale(double priceAtSale) {
        this.priceAtSale = priceAtSale;
    }
}
