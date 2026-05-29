package com.example.kiosk_register;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.RegisterDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ControllerDB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = new ControllerDB(getApplicationContext());
        List<Item> itemList = db.getActiveList();

        initiateButtons(itemList);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }



    public void initiateButtons(List<Item> itemList) {
        // get a List of all Buttons in the Grid to avoid using getIdentifier
        View grid = findViewById(R.id.buttonGrid);

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < ((ViewGroup) grid).getChildCount(); i++) {
            buttons.add((Button) ((ViewGroup) grid).getChildAt(i));
        }

        // set the text of all buttons according to the database entry
        for (int i = 0; i < itemList.size(); i++) {
            int buttonNumber = itemList.get(i).buttonNumber - 1;

            // in case we somehow have a faulty index in our database get rid of that here
            if (buttonNumber < 0 || buttonNumber >= buttons.size()) {
                Log.e("Kiosk_Register", "Ungültiger Datenbereich für ButtonNumber: " + buttonNumber);
                continue;
            }
            Button button = buttons.get(buttonNumber);

            button.setText(itemList.get(i).name);
        }

        db.disableEmptyButtons(buttons);
    }


}