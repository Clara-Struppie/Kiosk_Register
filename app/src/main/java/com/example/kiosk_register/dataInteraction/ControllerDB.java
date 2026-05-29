package com.example.kiosk_register.dataInteraction;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.RegisterDatabase;

import java.util.List;

public class ControllerDB {
    RegisterDatabase db;
    public ControllerDB(Context context) {
        db = Room.databaseBuilder(
                context,
                RegisterDatabase.class,
                "kiosk_register"
        ).createFromAsset("database/kiosk_register.db").allowMainThreadQueries().build();
    }

    public List<Item> getActiveList() {
        return db.itemDao().getActiveItems();
    }

    public void toggle(View v) {
        v.setEnabled(false);
    }

    public void disableEmptyButtons(@NonNull List<Button> buttons) {
        for (Button button: buttons) {
            if(button.getText().equals("")) {
                toggle(button);
            }
        }
    }
}
