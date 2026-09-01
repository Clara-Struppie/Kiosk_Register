package com.example.kiosk_register;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_register.dataInteraction.ControllerDB;
import com.example.kiosk_register.database.Sale;
import com.example.kiosk_register.database.SoldItemWithName;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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
    private String chosenDate = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_overview);

        controllerDB = new ControllerDB(this);

        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        long currentDayMillis = date.getTime();

        App.DB_EXECUTOR.execute(() -> {
            salesList = controllerDB.getLatestSales(currentDayMillis);

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
                chosenDate = dateStrings[i];
                displaySoldItems(chosenDate);
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

    /*
    Creates a sorted Map containing item names and the quantity of the corresponding item sold
     */
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

    private static final int CREATE_CSV_FILE = 100;

    public void exportStatistics(View view) {
        if (soldItemsMap == null || soldItemsMap.isEmpty()) {
            Toast.makeText(this, "Keine Verkaufsdaten zum Exportieren vorhanden.", 0).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        String dateString = chosenDate.replace(".", "-");
        intent.putExtra(Intent.EXTRA_TITLE, "Verkaufsstatistik_" + dateString + ".csv");

        startActivityForResult(intent, CREATE_CSV_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_CSV_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri == null) {
                return;
            }

            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);

                if (outputStream == null) {
                    throw new IOException("Datei konnte nicht geöffnet werden.");
                }

                OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

                writer.write("Verkaufsstatistik;" + chosenDate + "\n");
                writer.write("Artikel;Menge\n");

                for (Map.Entry<String, Integer> entry : soldItemsMap.entrySet()) {
                    //make sure there are no unwanted symbols in the String
                    writer.write(escapeCsv(entry.getKey()));
                    writer.write(";");
                    writer.write(String.valueOf(entry.getValue()));
                    writer.write("\n");
                }

                writer.flush();
                writer.close();

                Toast.makeText(this, "Verkaufsstatistik für " + chosenDate + " exportiert.", 1).show();
            } catch (IOException e) {
                e.printStackTrace();

                Toast.makeText(this, "Fehler beim Exportieren der Verkaufsstatistik", 1).show();
            }
        }
    }

    /*
    Used to replace escape \ symbols that might have snuck into item names
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(";")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
