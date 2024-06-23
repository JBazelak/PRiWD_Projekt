package com.example.myapplication;



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button buttonPodazanie;
    private Button buttonRozmowa;
    private Button buttonRozpoznawanie;
    private ImageButton buttonBack;
    private Switch switchProjektor;
    private Switch switchSwiatlo;
    private Switch switchAnimacjaZycia;
    private Switch switchPad;
    private Switch switchPolaczniePC;
    private TextView textViewBatteryStatus;
    private ProgressBar progressBarBattery;
    private ImageView imageViewLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPodazanie = findViewById(R.id.buttonPodazanie);
        buttonRozmowa = findViewById(R.id.buttonRozmowa);
        buttonRozpoznawanie = findViewById(R.id.buttonRozpoznawanie);
        buttonBack = findViewById(R.id.buttonBack);
        switchProjektor = findViewById(R.id.switchProjektor);
        switchSwiatlo = findViewById(R.id.switchSwiatlo);
        switchAnimacjaZycia = findViewById(R.id.switchAnimacjaZycia);
        switchPad = findViewById(R.id.switchPad);
        switchPolaczniePC = findViewById(R.id.switchPolaczniePC);
        textViewBatteryStatus = findViewById(R.id.textViewBatteryStatus);
        progressBarBattery = findViewById(R.id.progressBarBattery);
        imageViewLogo = findViewById(R.id.imageViewLogo);

        // Add functionality to your buttons and switches here
    }
}
