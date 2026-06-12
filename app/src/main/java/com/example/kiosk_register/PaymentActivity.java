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

import java.math.BigDecimal;

public class PaymentActivity extends AppCompatActivity {
    private double totalPrice = 0.0;
    private double pricePaid = 0.0;
    String decimalString = "";
    private double changeDue = 0.0;
    private boolean decimalActivated = false;

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
    Change calculation followed by automatical display adjustment
     */
    private void calculateChange() {
        changeDue = pricePaid - totalPrice;
        adjustPaymentDisplay();
        checkForMatchingPayment();
    }

    /*
    Checks if the paid amount is enough so the transaction can be completed
     */
    private void checkForMatchingPayment() {
        Button payButton = findViewById(R.id.completionButton);
        payButton.setEnabled(pricePaid >= totalPrice);
    }

    /*
    Increases the paid amount according to the typed numbers of the numpad.
    If decimal state is set to true, increases the decimal numbers
     */
    public void increasePaidAmount(View view) {
        String valueString = view.getTag().toString();

        if (decimalActivated) {
            boolean hasTwoDecimalPoints = checkDecimalLength(decimalString);
            if (hasTwoDecimalPoints) {
                return;
            }
        }
        decimalString += valueString;
        pricePaid = Double.parseDouble(decimalString);
        calculateChange();
    }

    /*
    Checks if there are two or more numbers after the decimal point
     */
    private boolean checkDecimalLength(String decimalString) {
        return decimalString.length() - decimalString.indexOf('.') >= 3;
    }

    /*
    resets the current calculation of payment
     */
    public void resetPaidAmount(View view) {
        pricePaid = 0.0;
        decimalString = "";
        decimalActivated = false;
        calculateChange();
    }

    /*
    sets the decimal activation to true so post decimal point values can be typed in
     */
    public void setDecimalToTrue(View view) {
        decimalActivated = true;
        decimalString += ".";
    }

    /*
    Finishes this activity after the transaction has been made
     */
    public void finishPayment(View view) {
        finish();
    }
}