package com.example.kiosk_register;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.constraintlayout.helper.widget.Flow;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.Sale;
import com.example.kiosk_register.database.SoldItem;
import com.google.android.material.internal.FlowLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ControllerDB controllerDB;
    private HashMap<Integer, Item> itemList;
    private HashMap<Integer, Integer> shoppingCart;
    private double currentTotal = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the DB Controller, itemList and shoppingCart. Also sets the display of the Total

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

    /*
    Creates a HashMap of all items from the database who's "active" status is set to true. This itemList
    is used globally to quickly access information about the items without having to rely on multiple
    database queries every time.
     */
    @NonNull
    private HashMap<Integer, Item> createItemList() {
        List<Item> list = controllerDB.getActiveList();
        HashMap<Integer, Item> itemMap = new HashMap<>();
        for (Item item: list) {
            itemMap.put(item.getId(), item);
        }
        return itemMap;
    }

    /*
    This function initiates the buttons for the kiosk items by assigning each item to their corresponding
    button determined in the database. If there is an unused button at the end, it gets disabled.
     */
    private void initiateButtons() {
        // get a List of all Buttons in the Grid to avoid using getIdentifier
        Flow grid = findViewById(R.id.buttonGrid);

        List<Button> buttons = new ArrayList<>();

        for (int i: grid.getReferencedIds()) {
            buttons.add(findViewById(i));
        }
        // set the text of all buttons according to the database entry
        for (Integer i: itemList.keySet()) {
            int buttonNumber = itemList.get(i).getButtonNumber() - 1;

            // in case we somehow have a faulty index in our database get rid of that here
            if (buttonNumber < 0 || buttonNumber >= buttons.size()) {
                Log.e("Kiosk_Register", "Ungültiger Datenbereich für ButtonNumber: " + buttonNumber);
                continue;
            }
            Button button = buttons.get(buttonNumber);

            String price = String.format("%.2f", itemList.get(i).getPrice());
            String buttonText = itemList.get(i).getName() +
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
    private void toggle(@NonNull View v) {
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
            Item item = (Item) button.getTag();

            // add item to shopping cart hash map
            increaseCartTotal(item.getId());

            writeReceiptLine(item);

            adjustTotal();
        }
    }

    /*
    Manages the display of each unique item position in the receipt on the right side of the Activity.
    If this is the first instance of an item being added to the cart, this method creates a new box
    (TableLayout) and fills it with the name, price, count and total of that position.
    If this is another instance of an already existing item, we instead increase the count and total.
     */
    private void writeReceiptLine(@NonNull Item item) {
        ViewGroup layout = findViewById(R.id.cartLayout);

        String name = item.getName();
        String price = String.format("%.2f", item.getPrice());
        String count = shoppingCart.get(item.getId()).toString();
        String topRowTag = name + "topRow";
        String bottomRowTag = name + "bottomRow";

        if (shoppingCart.get(item.getId()) == 1) {
            // sets the display of the item name plus it's total count
            TextView nameText = new TextView(this);
            nameText.setTag("nameText");
            nameText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ));
            nameText.setTextSize(20);
            String fullNameText = count + " " + name;
            nameText.setText(fullNameText);

            // sets the price total at the end of the top row next to the name display
            TextView priceText = new TextView(this);
            priceText.setTag("priceText");
            priceText.setGravity(Gravity.END);
            priceText.setTextSize(20);
            priceText.setText(price);

            // top row includes the following information: count, name and total position price
            LinearLayout topRow = createRow();
            topRow.setTag(topRowTag);
            topRow.addView(nameText);
            topRow.addView(priceText);

            // bottom row displays the total position count and price of a single item
            String bottomText = "\t" + count + "x " + price;
            TextView bottomRow = new TextView(this);
            bottomRow.setTag(bottomRowTag);
            bottomRow.setText(bottomText);

            TableLayout block = createTableLayout(item);
            block.setBackgroundColor(Color.parseColor("#FFF4F4"));
            block.addView(topRow);
            block.addView(bottomRow);

            layout.addView(block);

        } else {
            // adjusts the count in the top row
            LinearLayout topRow = layout.findViewWithTag(topRowTag);
            TextView nameText = topRow.findViewWithTag("nameText");
            String newText = count + " " + name;
            nameText.setText(newText);

            // adjusts the total in the top row
            TextView priceText = topRow.findViewWithTag("priceText");
            double totalPrice = item.getPrice() * shoppingCart.get(item.getId());
            String newPrice = String.format("%.2f", totalPrice);
            priceText.setText(newPrice);

            // adjusts count in the bottom row
            TextView bottomRow = layout.findViewWithTag(bottomRowTag);
            String bottomText = "\t" + count + "x " + price;
            bottomRow.setText(bottomText);
        }
    }

    /*
    Creates a new row for the TableLayout in horizontal orientation. Height is set to adjust to content.
     */
    @NonNull
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
    Creates the TableLayout (box) for each item position in the receipt. Also creates an OnLongClickListener
    to remove the item from the position after a long click.
     */
    @NonNull
    private TableLayout createTableLayout(Item item) {
        TableLayout block = new TableLayout(this);
        block.setOrientation(LinearLayout.HORIZONTAL);
        block.setPadding(10,10,10,40);
        block.setBackgroundColor(0xffffffff);
        block.setTag(item);
        block.setLongClickable(true);

        block.setOnLongClickListener(v -> {
            removePosition((LinearLayout) v);
            return true;
        });

        block.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View view) {
                removeSingleItem(view);
            }
        });

        return block;
    }

    /*
    Adjusts the display of the total price of the current cart at the bottom right of the Activity.
    If cart is empty, the display is set to 0.00€, otherwise to the current total of the cart.
     */
    private void adjustTotal() {
        TextView totalView = findViewById(R.id.totalText);
        if (shoppingCart.isEmpty()) {
            String totalText = "Total: 0.00 €";
            totalView.setText(totalText);
        } else {
            currentTotal = 0.00;
            for (Integer i : shoppingCart.keySet()) {
                double price = itemList.get(i).getPrice();
                if (shoppingCart.containsKey(i)) {
                    int countItem = shoppingCart.get(i);
                    currentTotal += countItem * price;
                }
            }

            double finalTotal = currentTotal;

            String totalPrice = String.format("%.2f", finalTotal);
            String totalText = "Total: " + totalPrice + "€";
            totalView.setText(totalText);
        }
    }

    /*
    Increases the count of an item in the cart. If this item is added for the first time, this creates
    a new key - value - pair.
     */
    private void increaseCartTotal(int itemID) {
        if (shoppingCart.containsKey(itemID)) {
            shoppingCart.put(itemID, shoppingCart.get(itemID) + 1);
        } else {
            shoppingCart.put(itemID, 1);
        }
    }

    /*
    Toggles the "Pay"-button in the bottom right corner of the Activity to true.
     */
    private void activatePayment() {
        View payButton = findViewById(R.id.payButton);
        if (!payButton.isEnabled()) {
            toggle(payButton);
        }
    }

    /*
    Completely removes an item from the shopping cart and from the receipt.
     */
    private void removePosition(@NonNull LinearLayout row) {
        row.removeAllViews();
        ViewGroup parent = (ViewGroup) row.getParent();
        parent.removeView(row);
        @NotNull Item item;
        item = (Item) row.getTag();
        Integer itemID = item.getId();
        shoppingCart.remove(itemID);
        adjustTotal();
    }

    /*
    Reduces the count of an item position by one. If the position count is at 1 it removes the entire position
     */
    private void removeSingleItem(View box) {
        Log.d("TEST", "DoubleClick detected!!!");
        @NotNull Item item;
        item = (Item) box.getTag();
        Integer itemID = item.getId();
        if (shoppingCart.get(itemID) > 1) {
            Integer oldCount = shoppingCart.get(itemID);
            int newCount = oldCount - 1;
            double price = item.getPrice();
            shoppingCart.put(itemID, newCount);
            adjustTotal();

            String newCountString = String.valueOf(newCount);
            String name = item.getName();
            String priceString = String.format("%.2f", price);
            String topRowTag = name + "topRow";
            String bottomRowTag = name + "bottomRow";

            // adjusts the count in the top row
            LinearLayout topRow = box.findViewWithTag(topRowTag);
            TextView nameText = topRow.findViewWithTag("nameText");
            String newText = newCountString + " " + name;
            nameText.setText(newText);

            // adjusts the total in the top row
            TextView priceText = topRow.findViewWithTag("priceText");
            double totalPrice = price * newCount;
            String newPrice = String.format("%.2f", totalPrice);
            priceText.setText(newPrice);

            // adjusts count in the bottom row
            TextView bottomRow = box.findViewWithTag(bottomRowTag);
            String bottomText = "\t" + newCountString + "x " + priceString;
            bottomRow.setText(bottomText);
        } else {
            removePosition((LinearLayout) box);
        }
    }

    /*
    Sends Sale to the database to be saved, then empties shopping cart in front and backend
     */
    public void saveSale(View view) {
        Sale sale = new Sale();
        sale.setTimestamp(System.currentTimeMillis());
        sale.setTotal(currentTotal);
        App.DB_EXECUTOR.execute(() -> {
            controllerDB.saveSale(sale);
            saveSoldItems(sale);
        });
        /*
        TO DO:
        Method to empty shopping cart, empty ScrollView, set Total to 0.00
         */
    }

    public void saveSoldItems(Sale sale) {
        App.DB_EXECUTOR.execute(() -> {
            int saleID = controllerDB.getRecentSaleID(sale.getTimestamp());
            for (Integer itemID : shoppingCart.keySet()) {
                double currentPrice = itemList.get(itemID).getPrice();
                SoldItem soldItem = new SoldItem();
                soldItem.setItemID(itemID);
                soldItem.setSaleID(saleID);
                soldItem.setQty(shoppingCart.get(itemID));
                soldItem.setPriceAtSale(currentPrice);
                controllerDB.saveSoldItem(soldItem);
            }
            runOnUiThread(() -> {
                startPaymentScreen();
                clearCart();
                adjustTotal();
            });
        });
    }

    private void clearCart() {
        shoppingCart = new HashMap<>();
        ViewGroup layout = findViewById(R.id.cartLayout);
        layout.removeAllViews();
    }

    private void startPaymentScreen() {
        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
        intent.putExtra("totalKey", currentTotal);
        MainActivity.this.startActivity(intent);
    }

    /*
    Class for Double Tab implementation by geeksforgeeks https://www.geeksforgeeks.org/android/double-tap-on-a-button-in-android/
     */
    public abstract static class DoubleClickListener implements View.OnClickListener {
        private long lastClickTime = 0;

        @Override
        public void onClick(View view) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(view);
            }
            lastClickTime = clickTime;
        }
        public abstract void onDoubleClick(View view);
        private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    }
}


