package com.example.kiosk_register;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.RegisterDatabase;

import java.util.List;

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
                "kiosk_register"
        ).createFromAsset("database/kiosk_register.db").allowMainThreadQueries().build();

/*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

 */
    }

    public void toggle(View v) {
        v.setEnabled(false);
        TextView t = findViewById(R.id.hello);
        t.setText("Goodbye, cruel world!");
        Button b = (Button) v;
        b.setText("Ich bin weg :c");
    }
}