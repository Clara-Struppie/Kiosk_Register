package com.example.kiosk_register.database;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Item.class, Sale.class, SoldItem.class},
        version = 3,
        autoMigrations = {
                @AutoMigration(from = 2, to = 3)
        }
)
public abstract class RegisterDatabase extends RoomDatabase {

    private static RegisterDatabase instance;
    public abstract ItemDao itemDao();
    public abstract SaleDao saleDao();
    public abstract SoldItemDao soldItemDao();

    public static synchronized RegisterDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    RegisterDatabase.class,
                    "kiosk_register"
            ).createFromAsset("database/kiosk_register.db").build();
        }
        return instance;
    }
}