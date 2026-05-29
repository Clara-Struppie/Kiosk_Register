package com.example.kiosk_register;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    private ControllerDB controllerDB;
    private List<Item> itemList;
    private HashMap<Integer, Integer> shoppingCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // initiates database controller and buttons
        controllerDB = new ControllerDB(this);

        App.DB_EXECUTOR.execute(() -> {
            itemList = controllerDB.getActiveList();

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



    /*
    This function initiates the buttons for the kiosk items by assigning each item to their corresponding
    button determined in the database. If there is an unused button at the end, it gets disabled.
     */
    private void initiateButtons() {
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

            String price = String.format("%.2f", itemList.get(i).price);
            String buttonText = itemList.get(i).name +
                    System.lineSeparator() +
                    System.lineSeparator() +
                    System.lineSeparator() +
                    price + "€";
            button.setText(buttonText);
            button.setTag(itemList.get(i).id);
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

    /*
    Adds new item to cart. In case of the price changing since being stored at the start of the activity,
    we use a db request instead of looking it up right in the list or storing the item itself as a tag.
     */
    public void addToCart(View view) {
        Button button = (Button) view;
        activatePayment();
        if (button != null) {
            ViewGroup layout = findViewById(R.id.cartLayout);

            // Create a row with two entries:
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            TextView nameView = new TextView(this);
            TextView priceView = new TextView(this);

            nameView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            priceView.setGravity(Gravity.END);

            // add item to shopping cart hash map
            int itemID = (int) button.getTag();
            increaseCartTotal(itemID);

            App.DB_EXECUTOR.execute(() -> {
                Item item = controllerDB.getItemByID(itemID);
                runOnUiThread(() -> {

                    String price = String.format("%.2f", item.price);
                    String name = item.name;
                    nameView.setText(name);
                    priceView.setText(price + "€");
                    row.addView(nameView);
                    row.addView(priceView);

                    layout.addView(row);
                    adjustTotal();
                });
            });
        }
    }

    private void adjustTotal() {
        TextView totalView = findViewById(R.id.totalText);
        if (shoppingCart.isEmpty()) {
            String totalText = "Total: 0.00 €";
            totalView.setText(totalText);
        } else {
            App.DB_EXECUTOR.execute(() -> {
                List<Item> items = controllerDB.getActiveList();
                double total = 0.0;

                for (Integer i : shoppingCart.keySet()) {
                    double price = items.get(i).price;
                    if (shoppingCart.containsKey(i)) {
                        int countItem = shoppingCart.get(i);
                        total += countItem * price;
                    }
                }

                double finalTotal = total;

                runOnUiThread(() -> {
                    String totalPrice = String.format("%.2f", finalTotal);
                    String totalText = "Total: " + totalPrice + "€";
                    totalView.setText(totalText);
                });

            });
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
}


