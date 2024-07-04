package com.example.polinav3.AI_prediction;

import android.os.Bundle;

import com.example.polinav3.R;
import com.sanbot.opensdk.base.TopBaseActivity;

public class Recognition extends TopBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(Recognition.class);
        setContentView(R.layout.activity_recognition);

    }

    @Override
    protected void onMainServiceConnected() {

    }
}