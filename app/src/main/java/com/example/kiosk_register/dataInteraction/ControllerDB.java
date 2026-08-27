package com.example.kiosk_register.dataInteraction;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.example.kiosk_register.database.Item;
import com.example.kiosk_register.database.ItemDao;
import com.example.kiosk_register.database.RegisterDatabase;
import com.example.kiosk_register.database.Sale;
import com.example.kiosk_register.database.SaleDao;
import com.example.kiosk_register.database.SoldItem;
import com.example.kiosk_register.database.SoldItemDao;

import java.util.List;

public class ControllerDB {
    private final ItemDao itemDao;
    private final SaleDao saleDao;
    private final SoldItemDao soldItemDao;

    public ControllerDB(Context context) {
        RegisterDatabase db = RegisterDatabase.getInstance(context);

        itemDao = db.itemDao();
        saleDao = db.saleDao();
        soldItemDao = db.soldItemDao();
    }

    public List<Item> getActiveList() {
        return itemDao.getActiveItems();
    }
    public List<Item> getFullItemList() {
        return itemDao.getAllItems();
    }
    public void saveSale(Sale sale) {
        saleDao.insert(sale);
    }
    public int getRecentSaleID(long timestamp) {
        return saleDao.getRecentSaleID(timestamp);
    }
    public void saveSoldItem(SoldItem item) {
        soldItemDao.insert(item);
    }
    public Item getItemByID(int id) {
        return itemDao.getItemByID(id);
    }
    public void saveItem(Item item) {
        itemDao.insert(item);
    }
    public void updateItem (Item item) {
        itemDao.update(item);
    }
}
