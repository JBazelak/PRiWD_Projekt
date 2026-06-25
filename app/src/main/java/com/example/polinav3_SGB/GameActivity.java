package com.example.polinav3_SGB;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.beans.EmotionsType;
import com.sanbot.opensdk.function.beans.FaceRecognizeBean;
import com.sanbot.opensdk.function.beans.LED;
import com.sanbot.opensdk.function.beans.wing.AbsoluteAngleWingMotion;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.MediaManager;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;
import com.sanbot.opensdk.function.unit.interfaces.media.FaceRecognizeListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends TopBaseActivity{

    List<byte[]> images = new ArrayList<>();
    Button playGameButton = findViewById(R.id.playGameButton);
    TextView textView = findViewById(R.id.textView);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private HDCameraManager hdCameraManager;
    private SystemManager systemManager;
    private HardWareManager hardWareManager;
    private SpeechManager speechManager;
    private WingMotionManager wingMotionManager;
    private AbsoluteAngleWingMotion AbsoluteAngleWingMotion;
    private int streamHandle = -1;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    StringBuilder stringBuilder = new StringBuilder();
    String[] speechBubbles = {"Ma", "Ry", "Na", "Rzyk!"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_game);

        playGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Zagrajmy!");
                speechManager.startSpeak("Zagrajmy!");
                AbsoluteAngleWingMotion absoluteAngleWingMotion = new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_RIGHT,5,80);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        wingMotionManager.doAbsoluteAngleMotion(absoluteAngleWingMotion);
                        images.clear();
                        for (int i = 0; i<4; i++){
                            try {
                                images.add(captureSingleFrame());
                                textView.setText(speechBubbles[i]);
                                speechManager.startSpeak(speechBubbles[i]);
                                wingMotionManager.doAbsoluteAngleMotion(absoluteAngleWingMotion);
                                if (i%2 == 0){
                                    wingMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_RIGHT,5,90));
                                } else {
                                    wingMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_RIGHT,5,80));
                                }
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        KlientApi klientApi = new KlientApi();
                        klientApi.sendFramesAndPlay(images, new KlientApi.GameCallback(){
                            @Override
                            public void onResult(String playerGesture, String robotGesture, String result) {
                                String message = "TY: " + playerGesture + " | ROBOT: " + robotGesture + " -> " + result;
                                Log.d("SanBot", "WYNIK SYMULACJI: " + message);
                                getRobotReaction(result);
                                speechManager.startSpeak("Zagrajmy jeszcze raz!");
                                textView.setText("Koniec gry zagrajmy jeszcze raz! ↑KLIKNIJ↑");
                            }

                            @Override
                            public void onError(String errMessage) {
                                Log.e("SanBot", "BŁĄD SYMULACJI: " + errMessage);
                                speechManager.startSpeak(errMessage);
                            }

                        });

                    }
                });

            }
        });
    }

    @Override
    protected void onMainServiceConnected() {
        hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
        systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
        hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
        speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        wingMotionManager = (WingMotionManager) getUnitManager(FuncConstant.WINGMOTION_MANAGER);
        StreamOption streamOption = new StreamOption();
        streamOption.setChannel(StreamOption.MAIN_STREAM);
        streamOption.setDecodType(StreamOption.HARDWARE_DECODE);
        streamOption.setJustIframe(false);

        // Żądanie otwarcia strumienia
        OperationResult operationResult = hdCameraManager.openStream(streamOption);

        // Zapisanie uchwytu (handle) strumienia
        try {
            int result = Integer.parseInt(operationResult.getResult());
            if (result > -1) {
                streamHandle = result;
            }
        } catch (NumberFormatException e) {
            streamHandle = -1;
        }

    }

    public byte[] captureSingleFrame() {
        if (hdCameraManager != null) {
            // Złapanie klatki z otwartego strumienia
            Bitmap frame = hdCameraManager.getVideoImage();
            frame.compress(Bitmap.CompressFormat.JPEG, 70, stream);

            if (frame != null) {
                Log.d("Video capture: ", "Zarejestrowano dłoń");
                // Mamy poprawne zdjęcie dłoni!
                return stream.toByteArray();
            }
        }
        return null; // Nie udało się pobrać klatki
    }

    public void getRobotReaction(String result){
        byte slowDown = (byte) 6;
        byte randomCount = (byte) 0;

        if (result.equals("robot_wins")){
            systemManager.showEmotion(EmotionsType.SNICKER);
            hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_GREEN, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_GREEN, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_LEFT_HAND, LED.MODE_FLICKER_GREEN, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HAND, LED.MODE_FLICKER_GREEN, slowDown, randomCount));
            textView.setText("Aha! Wygrałem!");
            speechManager.startSpeak("Aha! Wygrałem!");
        }
        else if (result.equals("draw")){
            systemManager.showEmotion(EmotionsType.QUESTION);
            hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_YELLOW, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_YELLOW, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_LEFT_HAND, LED.MODE_FLICKER_YELLOW, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HAND, LED.MODE_FLICKER_YELLOW, slowDown, randomCount));
            textView.setText("Remis!");
            speechManager.startSpeak("Remis!");
        }
        else if (result.equals("player_wins")){
            systemManager.showEmotion(EmotionsType.ABUSE);
            hardWareManager.setLED(new LED(LED.PART_LEFT_HEAD, LED.MODE_FLICKER_RED, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HEAD, LED.MODE_FLICKER_RED, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_LEFT_HAND, LED.MODE_FLICKER_RED, slowDown, randomCount));
            hardWareManager.setLED(new LED(LED.PART_RIGHT_HAND, LED.MODE_FLICKER_RED, slowDown, randomCount));
            textView.setText("O nie! Przegrałem!");
            speechManager.startSpeak("O nie! Przegrałem!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Bezpieczne zamknięcie strumienia wideo przed zniszczeniem Activity
        if (hdCameraManager != null && streamHandle != -1) {
            hdCameraManager.closeStream(streamHandle);
        }
        wingMotionManager.doAbsoluteAngleMotion(new AbsoluteAngleWingMotion(AbsoluteAngleWingMotion.PART_RIGHT,5,0));
        executor.shutdown();
    }

}


