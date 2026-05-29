package com.example.kiosk_register;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    public static ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);
}
