package com.example.polinav3_SGB;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sanbot.opensdk.base.TopBaseActivity;

public class MainActivity extends TopBaseActivity{

    Intent testIntent = new Intent();
    Intent gameIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);

        Button goToTest = findViewById(R.id.buttonToTest);
        Button goToGame = findViewById(R.id.buttonToGame);

        goToTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testIntent.setClass(MainActivity.this, TestActivity.class);
                startActivity(testIntent);
            }
        });

        goToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameIntent.setClass(MainActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });
    }

    @Override
    protected void onMainServiceConnected() {

    }

}
