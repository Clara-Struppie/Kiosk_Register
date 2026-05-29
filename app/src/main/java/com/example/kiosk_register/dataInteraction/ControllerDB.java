package com.example.kiosk_register.dataInteraction;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.ItemDao;
import com.example.kiosk_register.database.RegisterDatabase;

import java.util.List;

public class ControllerDB {
    private final ItemDao itemDao;

    public ControllerDB(Context context) {
        RegisterDatabase db = RegisterDatabase.getInstance(context);

        itemDao = db.itemDao();
    }

    public Item getItemByID(int itemID) {
        return itemDao.getItemByID(itemID);
    }
    public List<Item> getActiveList() {
        return itemDao.getActiveItems();
    }


}
