package com.example.kiosk_register.database;

public class SoldItemWithName {
    private String name;
    private int quantity;

    public SoldItemWithName (String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
