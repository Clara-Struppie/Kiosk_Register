package com.example.kiosk_register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Sale;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class SaleOverviewActivity extends AppCompatActivity {

    private ControllerDB controllerDB;
    private List<Sale> salesList;
    private List<Date> dateList;
    private TreeMap<Long, List<Integer>> dateMap;


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
        for (Sale sale : salesList) {
            Long timestamp = sale.getTimestamp();
            Integer saleID = sale.getId();
            if (!dateMap.containsKey(timestamp)) {
                dateMap.put(timestamp, new ArrayList<>(saleID));
            } else {
                List<Integer> idList = dateMap.get(timestamp);
                idList.add(saleID);
                dateMap.put(timestamp, idList);
            }
        }
    }

    /*
    Fills dropDown menu with all recent 30 day sale dates (keys from dateMap)
     */
    private void setUpDropdown() {
        AutoCompleteTextView dropdown = findViewById(R.id.dropdown_saleDates);
        Long[] allDates = dateMap.keySet().toArray(new Long[0]);
        List<String> dateStrings = new ArrayList<>();
        for (Long timestamp : allDates) {
            Date date = new Date(timestamp);
            String dateString = date.toString();
            dateStrings.add(dateString);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dateStrings);
        dropdown.setAdapter(adapter);
    }
}
