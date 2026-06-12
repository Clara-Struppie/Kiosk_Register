package com.example.kiosk_register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentActivity extends AppCompatActivity {
    private double totalPrice = 0.0;
    private double pricePaid = 0.0;
    private double changeDue = 0.0;
    private boolean commaActivated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        this.totalPrice = intent.getDoubleExtra("totalKey", 0.0);

        setStartingDisplays();
    }

    /*
    Initiates the starting displays of the total price, paid amount and change due.
     */
    private void setStartingDisplays() {

        // total price display
        TextView totalView = findViewById(R.id.textTotal);
        String totalString = String.format("%.2f", totalPrice);
        String totalText = totalString + " €";
        totalView.setText(totalText);

        // paid amount and change display
        adjustPaymentDisplay();
    }

    /*
    Adjusts the display of the paid and change amount to reflect the actual current value
     */
    private void adjustPaymentDisplay() {

        // paid amount display
        TextView paidView = findViewById(R.id.textPaid);
        String paidString = String.format("%.2f", pricePaid);
        String paidText = paidString + " €";
        paidView.setText(paidText);

        // change due display
        TextView changeView = findViewById(R.id.textChange);
        String changeString = String.format("%.2f", changeDue);
        String changeText = changeString + " €";
        changeView.setText(changeText);
    }

    /*
    Increases paid amount by a fixed value given by the corresponding banknote
     */
    public void setBanknoteValue (View view) {
        String valueString = view.getTag().toString();
        int noteValue = Integer.parseInt(valueString);
        pricePaid += noteValue;
        calculateChange();
    }

    /*
    Sets amount paid to the total price
     */
    public void exactPayment(View view) {
        pricePaid = totalPrice;
        calculateChange();
    }

    /*
    Change calculation
     */
    private void calculateChange() {
        changeDue = pricePaid - totalPrice;
        adjustPaymentDisplay();
        checkPaymentAmount();
    }

    /*
    Checks if the paid amount is enough so the transaction can be completed
     */
    private void checkPaymentAmount() {
        Button payButton = findViewById(R.id.completionButton);
        payButton.setEnabled(pricePaid >= totalPrice);
    }

    public void finishPayment(View view) {
        finish();
    }
}