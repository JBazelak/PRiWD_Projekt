package com.example.polinav3_SGB;

import android.os.Bundle;
import com.sanbot.opensdk.base.TopBaseActivity;

public class GameActivity extends TopBaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onMainServiceConnected() {

    }
}
