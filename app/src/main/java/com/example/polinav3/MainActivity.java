package com.example.polinav3;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.unit.SpeechManager;

public class MainActivity extends TopBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NetworkServer net = new NetworkServer();
        net.start();
    }

    @Override
    protected void onMainServiceConnected() {
        SpeechManager sm = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        if (sm != null) {
            sm.startSpeak("Dzień dobry dzieciątka!");
        }
    }
}