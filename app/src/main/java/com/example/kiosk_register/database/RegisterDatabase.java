package com.example.kiosk_register.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Item.class, Sale.class, SoldItem.class},
        version = 1
)
public abstract class RegisterDatabase extends RoomDatabase {
    // TT__TT
    public abstract ItemDao itemDao();
    public abstract SaleDao saleDao();
    public abstract SoldItem soldItem();
}