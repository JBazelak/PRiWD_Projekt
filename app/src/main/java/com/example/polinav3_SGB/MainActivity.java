package com.example.polinav3_SGB;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.function.unit.HardWareManager;

public class MainActivity extends TopBaseActivity{

    Intent testIntent = new Intent();
    Intent gameIntent = new Intent();

    public HardWareManager hardWareManager;
    public boolean isFlashlightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);

        Button goToTest = findViewById(R.id.buttonToTest);
        Button goToGame = findViewById(R.id.buttonToGame);
        Button switchFlashlight = findViewById(R.id.buttonFlashlight);

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

//        switchFlashlight.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isFlashlightOn) {
////                    hardWareManager.switchWhiteLight(false);
//                    isFlashlightOn = false;
//                    System.out.println("flsh is off");
//                } else {
////                    hardWareManager.switchWhiteLight(true);
//                    isFlashlightOn = true;
//                    System.out.println("Flashlight is on");
//                }
//            }
//        });
    }

    @Override
    protected void onMainServiceConnected() {

    }

}
