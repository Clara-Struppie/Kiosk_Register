package com.example.kiosk_register;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.RegisterDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //DB initialisation
        RegisterDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                RegisterDatabase.class,
                "register-db"
        ).allowMainThreadQueries().build();

        Item item = new Item();
        item.name = "Vita Cola Pur";
        item.price = 4.50;
        item.active = true;

        db.itemDao().insert(item);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}