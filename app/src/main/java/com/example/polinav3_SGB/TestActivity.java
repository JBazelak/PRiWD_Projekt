package com.example.polinav3_SGB;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.HardWareManager;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends TopBaseActivity {

    private SystemManager systemManager;
    private HardWareManager hardWareManager;
    private SpeechManager speechManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Zmień MainActivity.class na TestActivity.class, jeśli to jest ten kontekst
        register(TestActivity.class);
        setContentView(R.layout.activity_test);

        Button predictionAndSerwerTestButton = findViewById(R.id.buttonTest);
        Button aDraw = findViewById(R.id.buttonDrew);
        Button robotLost = findViewById(R.id.buttonLost);

        systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
        hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);

        // Nadpisaliśmy tu listener, aby podpiąć symulację
        predictionAndSerwerTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SanBot", "Rozpoczynam symulację wysyłki klatek...");
                simulateSendingFrames();

                // Opcjonalne włączenie dawnych akcji:
                // systemManager.showEmotion(EmotionsType.SNICKER);
                // hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_GREEN));
                // speechManager.startSpeak("Aha! Wygrałem!");
            }
        });

        aDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systemManager.showEmotion(EmotionsType.QUESTION);
                hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_YELLOW));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_YELLOW));
                hardWareManager.setLED(new LED(LED.PART_LEFT_HAND, LED.MODE_FLICKER_YELLOW));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HAND, LED.MODE_FLICKER_YELLOW));
                speechManager.startSpeak("Remis!");
            }
        });

        robotLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systemManager.showEmotion(EmotionsType.ABUSE);
                hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_RED));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_RED));
                hardWareManager.setLED(new LED(LED.PART_LEFT_HAND, LED.MODE_FLICKER_RED));
                hardWareManager.setLED(new LED(LED.PART_RIGHT_HAND, LED.MODE_FLICKER_RED));
                speechManager.startSpeak("O nie! Przegrałem!");
            }
        });
    }

    private void simulateSendingFrames() {
        List<byte[]> framesToSend = new ArrayList<>();

        // Upewnij się, że masz pliki 'test_rock.jpg' i 'test_paper.jpg' w res/drawable/
        Bitmap frame1 = BitmapFactory.decodeResource(getResources(), R.drawable.test1);
        Bitmap frame2 = BitmapFactory.decodeResource(getResources(), R.drawable.test2);
        Bitmap frame3 = BitmapFactory.decodeResource(getResources(), R.drawable.test3);

        if (frame1 == null || frame2 == null || frame3 == null) {
            Log.e("SanBot", "Nie znaleziono plików w folderze drawable!");
            return;
        }

        framesToSend.add(ImageUtils.encodeBitmapToJpeg(frame1));
        framesToSend.add(ImageUtils.encodeBitmapToJpeg(frame2));
        framesToSend.add(ImageUtils.encodeBitmapToJpeg(frame3));

        KlientApi gameClient = new KlientApi();

        // Używamy "KlientApi.GameCallback"
        gameClient.sendFramesAndPlay(framesToSend, new KlientApi.GameCallback() {
            @Override
            public void onResult(String playerGesture, String robotGesture, String result) {
                String message = "TY: " + playerGesture + " | ROBOT: " + robotGesture + " -> " + result;
                Log.d("SanBot", "WYNIK SYMULACJI: " + message);

                // Zmieniono na TestActivity.this
                Toast.makeText(TestActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("SanBot", "BŁĄD SYMULACJI: " + errorMsg);
                // Zmieniono na TestActivity.this
                Toast.makeText(TestActivity.this, "Błąd: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onMainServiceConnected() {
        // Tu można inicjalizować logikę powiązaną po połączeniu serwisu SDK SanBota
    }
}