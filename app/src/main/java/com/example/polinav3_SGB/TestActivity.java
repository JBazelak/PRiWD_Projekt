package com.example.polinav3_SGB;

import android.os.Bundle;
import com.sanbot.opensdk.base.TopBaseActivity;

public class TestActivity extends TopBaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onMainServiceConnected() {

    }
}
