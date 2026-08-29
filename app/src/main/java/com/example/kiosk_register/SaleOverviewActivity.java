package com.example.kiosk_register;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Sale;
import com.example.kiosk_register.database.SoldItemWithName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SaleOverviewActivity extends AppCompatActivity {

    private ControllerDB controllerDB;
    private List<Sale> salesList;
    private TreeMap<String, Integer> soldItemsMap;
    private TreeMap<String, List<Integer>> dateMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_overview);

        controllerDB = new ControllerDB(this);

        long currentTime = System.currentTimeMillis();
        App.DB_EXECUTOR.execute(() -> {
            salesList = controllerDB.getLatestSales(currentTime);

            runOnUiThread(() -> {
                fillDateMap();
                setUpDropdown();
            });
        });


    }

    /*
    fills the TreeMap with all Sales. Timestamps are keys, IDs of Sales get added to a list for the date
    this way we only have to query all specific sold items of a day when the day is selected
     */
    private void fillDateMap() {
        dateMap = new TreeMap<>();
        for (Sale sale : salesList) {
            String dateString = getDateString(sale);

            int saleID = sale.getId();
            List<Integer> idList;
            if (!dateMap.containsKey(dateString)) {
                idList = new ArrayList<>();
            } else {
                idList = dateMap.get(dateString);
            }
            idList.add(saleID);
            dateMap.put(dateString, idList);
        }
    }

    @NonNull
    private static String getDateString(Sale sale) {
        long timestamp = sale.getTimestamp();

        // make date from timestamp without any time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date day = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        return simpleDateFormat.format(day);
    }

    /*
    Fills dropdown menu with all recent 30 day sale dates (keys from dateMap)
     */
    private void setUpDropdown() {
        AutoCompleteTextView dropdown = findViewById(R.id.dropdown_saleDates);
        String[] dateStrings = dateMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dateStrings);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displaySoldItems(dateStrings[i]);
            }
        });
    }

    /*
    Shows all sold items of the specific date in a list similar to the shopping cart of the main activity
     */
    private void displaySoldItems(String date) {
        App.DB_EXECUTOR.execute(() -> {
            List<Integer> saleIDList = dateMap.get(date);
            List<SoldItemWithName> soldItemList = controllerDB.getSoldItemsFromSalesList(saleIDList);

            runOnUiThread(() -> {
                listSoldItems(soldItemList);
                implementListInUI();
            });
        });
    }

    private void listSoldItems(List<SoldItemWithName> soldItemList) {
        soldItemsMap = new TreeMap<>();
        for (SoldItemWithName item : soldItemList) {
            String itemName = item.getName();
            int qty = item.getQuantity();
            if (soldItemsMap.containsKey(itemName)) {
                qty += soldItemsMap.get(itemName);
            }
            soldItemsMap.put(itemName, qty);
        }
    }

    /*
    Display the sold items as a scroll list with name and quantity
     */
    private void implementListInUI() {
        ViewGroup layout = findViewById(R.id.statisticListLayout);
        layout.removeAllViews();

        for (Map.Entry<String, Integer> entry : soldItemsMap.entrySet()) {
            String name = entry.getKey();
            int qty = entry.getValue();

            TextView text = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            );
            layoutParams.setMargins(8,16,0,0);
            text.setLayoutParams(layoutParams);

            text.setTextSize(20);
            String fullText = name + ": " + qty;
            text.setText(fullText);

            layout.addView(text);
        }
    }

    public void cancelStatistics(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
