package com.example.kiosk_register;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ControllerDB controllerDB;
    private HashMap<Integer, Item> itemList;
    private HashMap<Integer, Integer> shoppingCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // initiates database controller and buttons
        controllerDB = new ControllerDB(this);

        App.DB_EXECUTOR.execute(() -> {
            itemList = createItemList();

            runOnUiThread(() -> {
                initiateButtons();
            });

        });
        shoppingCart = new HashMap<>();
        adjustTotal();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private HashMap<Integer, Item> createItemList() {
        List<Item> list = controllerDB.getActiveList();
        HashMap<Integer, Item> itemMap = new HashMap<>();
        for (Item item: list) {
            itemMap.put(item.id, item);
        }
        return itemMap;
    }

    /*
    This function initiates the buttons for the kiosk items by assigning each item to their corresponding
    button determined in the database. If there is an unused button at the end, it gets disabled.
     */
    private void initiateButtons() {
        // get a List of all Buttons in the Grid to avoid using getIdentifier
        ViewGroup grid = findViewById(R.id.buttonGrid);

        List<Button> buttons = new ArrayList<>();

        for (int i = 0; i < grid.getChildCount(); i++) {
            buttons.add((Button) grid.getChildAt(i));
        }
        // set the text of all buttons according to the database entry
        for (Integer i: itemList.keySet()) {
            int buttonNumber = itemList.get(i).buttonNumber - 1;

            // in case we somehow have a faulty index in our database get rid of that here
            if (buttonNumber < 0 || buttonNumber >= buttons.size()) {
                Log.e("Kiosk_Register", "Ungültiger Datenbereich für ButtonNumber: " + buttonNumber);
                continue;
            }
            Button button = buttons.get(buttonNumber);

            String price = String.format("%.2f", itemList.get(i).price);
            String buttonText = itemList.get(i).name +
                    System.lineSeparator() +
                    System.lineSeparator() +
                    System.lineSeparator() +
                    price + "€";
            button.setText(buttonText);
            button.setTag(itemList.get(i));
        }
        // next disable all unused buttons
        disableEmptyButtons(buttons);
    }

    /*
    Inverts a button's enabled state
     */
    private void toggle(View v) {
        v.setEnabled(!v.isEnabled());
    }

    /*
    Buttons without text are being disabled to be unusable
     */
    private void disableEmptyButtons(@NonNull List<Button> buttons) {
        for (Button button: buttons) {
            if(button.getText().equals("")) {
                toggle(button);
            }
        }
    }

    private LinearLayout createRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return row;
    }

    /*
    Adds new item to cart. In case of the price changing since being stored at the start of the activity,
    we use a db request instead of looking it up right in the list or storing the item itself as a tag.
     */
    public void addToCart(View view) {
        Button button = (Button) view;
        activatePayment();
        if (button != null) {
            Item item = (Item) button.getTag();

            // add item to shopping cart hash map
            increaseCartTotal(item.id);

            writeReceiptLine(item);

            adjustTotal();
        }
    }

    private void writeReceiptLine(Item item) {
        ViewGroup layout = findViewById(R.id.cartLayout);

        String name = item.name;
        String price = String.format("%.2f", item.price);
        String count = shoppingCart.get(item.id).toString();
        String topRowTag = name + "topRow";
        String bottomRowTag = name + "bottomRow";

        if (shoppingCart.get(item.id) == 1) {
            TextView nameText = new TextView(this);
            nameText.setTag("nameText");
            nameText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            nameText.setTextSize(20);
            String fullNameText = count + " " + name;
            nameText.setText(fullNameText);

            TextView priceText = new TextView(this);
            priceText.setTag("priceText");
            priceText.setGravity(Gravity.END);
            priceText.setTextSize(20);
            priceText.setText(price);

            LinearLayout topRow = createRow();
            topRow.setTag(topRowTag);
            topRow.addView(nameText);
            topRow.addView(priceText);

            TableLayout block = createTableLayout(item);
            block.addView(topRow);
            TextView bottomRow = new TextView(this);
            block.addView(bottomRow);

            String bottomText = "\t" + count + "x " + price;
            bottomRow.setTag(bottomRowTag);
            bottomRow.setText(bottomText);

            layout.addView(block);
        } else {
            LinearLayout topRow = layout.findViewWithTag(topRowTag);
            TextView nameText = topRow.findViewWithTag("nameText");
            String newText = count + " " + name;
            nameText.setText(newText);

            TextView priceText = topRow.findViewWithTag("priceText");
            double totalPrice = item.price * shoppingCart.get(item.id);
            String newPrice = String.format("%.2f", totalPrice);
            priceText.setText(newPrice);

            TextView bottomRow = layout.findViewWithTag(bottomRowTag);
            String bottomText = "\t" + count + "x " + price;
            bottomRow.setText(bottomText);
        }
    }

    @NonNull
    private TableLayout createTableLayout(Item item) {
        TableLayout block = new TableLayout(this);
        block.setOrientation(LinearLayout.HORIZONTAL);
        block.setPadding(5,5,5,5);
        block.setTag(item);
        block.setLongClickable(true);

        block.setOnLongClickListener(v -> {
            removePosition((LinearLayout) v);
            return true;
        });
        return block;
    }

    private void adjustTotal() {
        TextView totalView = findViewById(R.id.totalText);
        if (shoppingCart.isEmpty()) {
            String totalText = "Total: 0.00 €";
            totalView.setText(totalText);
        } else {
            double total = 0.0;

            for (Integer i : shoppingCart.keySet()) {
                double price = itemList.get(i).price;
                if (shoppingCart.containsKey(i)) {
                    int countItem = shoppingCart.get(i);
                    total += countItem * price;
                }
            }

            double finalTotal = total;

            String totalPrice = String.format("%.2f", finalTotal);
            String totalText = "Total: " + totalPrice + "€";
            totalView.setText(totalText);

        }
    }

    private void increaseCartTotal(int itemID) {
        if (shoppingCart.containsKey(itemID)) {
            shoppingCart.put(itemID, shoppingCart.get(itemID) + 1);
        } else {
            shoppingCart.put(itemID, 1);
        }
    }

    private void activatePayment() {
        View payButton = findViewById(R.id.payButton);
        if (!payButton.isEnabled()) {
            toggle(payButton);
        }
    }

    private void removePosition(LinearLayout row) {
        row.removeAllViews();
        ViewGroup parent = (ViewGroup) row.getParent();
        parent.removeView(row);
        Item item = (Item) row.getTag();
        if (item != null) {
            Integer itemID = item.id;
            shoppingCart.remove(itemID);
            adjustTotal();
        }
    }
}


