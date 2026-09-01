package com.example.kiosk_register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class ItemEditorActivity extends AppCompatActivity {

    public static final String EXTRA_BUTTON_NUMBER = "buttonNumber";
    public static final String EXTRA_ITEM_ID = "itemId";

    private ControllerDB controllerDB;

    private EditText nameEditText;
    private EditText priceEditText;
    private TreeMap<String, Item> fullItemMap;

    private int buttonNumber;
    private int itemID = -1;
    private boolean isActive = true;
    private Item oldItemEntry;

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

        loadAllItems();

        // check if this is to edit an Item or create a new one
        if (itemID != -1) {
            loadExistingItem();
        }

        findViewById(R.id.saveItemButton).setOnClickListener(v -> saveItem());
    }

    /*
    Used to load a list of all items from the DB to add it to the dropdown list in the activity
     */
    private void loadAllItems() {
        App.DB_EXECUTOR.execute(() -> {
            List<Item> itemList = controllerDB.getFullItemList();

            //add dummy entry for a new item
            Item newItemSlot = new Item();
            newItemSlot.setId(-1);
            newItemSlot.setButtonNumber(-1);
            itemList.add(newItemSlot);

            HashMap<String, Item> itemMap = new HashMap<>();
            for (Item item : itemList) {
                itemMap.put(item.getName(), item);
            }

            //Sort alphabetically by turning the hashmap into a treemap
            fullItemMap = new TreeMap<>(itemMap);

            runOnUiThread(() -> {
                setUpDropdown();
            });
        });
    }

    /*
    Fills the dropdown menu with the names of all items in the hashmap
     */
    private void setUpDropdown() {
        AutoCompleteTextView dropdown = findViewById(R.id.dropdown_menu);
        String[] itemNames = fullItemMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemNames);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayItemInfo(itemNames[i]);
            }
        });
    }

    private void loadExistingItem() {
        App.DB_EXECUTOR.execute(() -> {
            Item item = controllerDB.getItemByID(itemID);

            if (item != null) {
                runOnUiThread(() -> {
                    nameEditText.setText(item.getName());
                    priceEditText.setText(String.valueOf(item.getPrice()));
                    oldItemEntry = item;
                });
            }
        });
    }

    /*
    Get the activation or deactivation of an item from the switch
     */
    public void toggleActive(View view) {
        Switch switchButton = (Switch) view;
        isActive = switchButton.isChecked();
    }

    /*
    Displays info of the selected item from the dropdown menu and deselects old item info
     */
    public void displayItemInfo(String itemName) {
        Log.i("ItemEditorActivity", "display item info");
        itemID = fullItemMap.get(itemName).getId();
        String name = fullItemMap.get(itemName).getName();
        String price = String.valueOf(fullItemMap.get(itemName).getPrice());
        nameEditText.setText(name);
        priceEditText.setText(price);
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
            if (!isActive) {
                buttonNumber = -1;
            }

            // if new item is saved
            if (itemID == -1) {
                if (oldItemEntry != null) {
                    deactivateOldItem();
                }

                Item item = new Item();

                item.setName(name);
                item.setPrice(price);
                item.setButtonNumber(buttonNumber);
                item.setActive(isActive);

                controllerDB.saveItem(item);
            } else { //for edited items
                int oldID = oldItemEntry != null ? oldItemEntry.getId() : -1;
                if (itemID != oldID && oldID != -1) {
                    deactivateOldItem();
                }
                Item item = controllerDB.getItemByID(itemID);

                if (item != null) {
                    item.setName(name);
                    item.setPrice(price);
                    item.setButtonNumber(buttonNumber);
                    item.setActive(isActive);

                    controllerDB.updateItem(item);
                }
            }


            runOnUiThread(() -> {
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void deactivateOldItem() {
        oldItemEntry.setActive(false);
        oldItemEntry.setButtonNumber(-1);
        controllerDB.updateItem(oldItemEntry);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        view.clearFocus();
    }

    public void cancelEdit(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
