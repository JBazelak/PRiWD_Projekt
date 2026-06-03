package com.example.polinav3_SGB;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.HardWareManager;

import com.sanbot.opensdk.base.TopBaseActivity;


public class TestActivity extends TopBaseActivity{

    private SystemManager systemManager;
    private HardWareManager hardWareManager;
    private SpeechManager speechManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_test);

        Button robotWon = findViewById(R.id.buttonWon);
        Button aDraw = findViewById(R.id.buttonDrew);
        Button robotLost = findViewById(R.id.buttonLost);

        systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
        hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);


        robotWon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systemManager.showEmotion(EmotionsType.SNICKER);
                hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_GREEN));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_GREEN));
                speechManager.startSpeak("Aha! Wygrałem!");
            }
        });

        aDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systemManager.showEmotion(EmotionsType.QUESTION);
                hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_YELLOW));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_YELLOW));
                speechManager.startSpeak("Remis!");
            }
        });

        robotLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systemManager.showEmotion(EmotionsType.ABUSE);
                hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_RED));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_RED));
                speechManager.startSpeak("O nie! Przegrałem!");
            }
        });

    }

    @Override
    protected void onMainServiceConnected() {

    }
}
