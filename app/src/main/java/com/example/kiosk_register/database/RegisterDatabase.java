package com.example.kiosk_register.database;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {Item.class, Sale.class, SoldItem.class},
        version = 2,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }
)
public abstract class RegisterDatabase extends RoomDatabase {
    public abstract ItemDao itemDao();
    public abstract SaleDao saleDao();
    public abstract SoldItemDao soldItemDao();
}