package com.example.kiosk_register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Item;

public class ItemEditorActivity extends AppCompatActivity {

    public static final String EXTRA_BUTTON_NUMBER = "buttonNumber";
    public static final String EXTRA_ITEM_ID = "itemId";

    private ControllerDB controllerDB;

    private EditText nameEditText;
    private EditText priceEditText;

    private int buttonNumber;
    private int itemID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_editor);

        controllerDB = new ControllerDB(this);

        nameEditText = findViewById(R.id.nameEditText);
        priceEditText = findViewById(R.id.priceEditText);

        Intent intent = getIntent();

        buttonNumber = intent.getIntExtra(EXTRA_BUTTON_NUMBER, -1);
        itemID = intent.getIntExtra(EXTRA_ITEM_ID, -1);

        // check if this is to edit an Item or create a new one
        if (itemID != -1) {
            loadExistingItem();
        }

        findViewById(R.id.saveItemButton).setOnClickListener(v -> saveItem());
    }

    private void loadExistingItem() {
        App.DB_EXECUTOR.execute(() -> {
            Item item = controllerDB.getItemByID(itemID);

            if (item != null) {
                runOnUiThread(() -> {
                    nameEditText.setText(item.getName());
                    priceEditText.setText(String.valueOf(item.getPrice()));
                });
            }
        });
    }

    private void saveItem() {
        String name = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Bitte einen Namen eingeben!");
            return;
        }
        if (priceString.isEmpty()) {
            priceEditText.setError("Bitte Preis angeben!");
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceString.replace(',', '.'));
        } catch (NumberFormatException e) {
            priceEditText.setError("Ungültiger Preis");
            return;
        }

        App.DB_EXECUTOR.execute(() -> {
            // if new item is saved
            if (itemID == -1) {
                Item item = new Item();

                item.setName(name);
                item.setPrice(price);
                item.setButtonNumber(buttonNumber);
                // needs to be changed to a variable once deactivation of items is implemented
                item.setActive(true);

                controllerDB.saveItem(item);
            } else { //for edited items
                Item item = controllerDB.getItemByID(itemID);

                if (item != null) {
                    item.setName(name);
                    item.setPrice(price);
                    item.setButtonNumber(buttonNumber);
                    // to be implemented: item.setActive(active);

                    controllerDB.updateItem(item);
                }
            }

            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        });
    }
}
