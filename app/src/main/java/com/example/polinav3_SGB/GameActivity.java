package com.example.polinav3_SGB;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sanbot.opensdk.base.BindBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.HDCameraManager;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends BindBaseActivity {

    private HDCameraManager hdCameraManager;
    private int streamHandle = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        register(GameActivity.class);
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_game); // Podłącz swój interfejs użytkownika
    }

    @Override
    protected void onMainServiceConnected() {
        // Robot jest gotowy, otwieramy strumień kamery
        hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);

        StreamOption streamOption = new StreamOption();
        streamOption.setChannel(StreamOption.MAIN_STREAM);
        streamOption.setDecodType(StreamOption.HARDWARE_DECODE);
        streamOption.setJustIframe(false);

        OperationResult operationResult = hdCameraManager.openStream(streamOption);

        try {
            streamHandle = Integer.parseInt(operationResult.getResult());
            Log.d("SanBot", "Strumień kamery otwarty pomyślnie.");
        } catch (NumberFormatException e) {
            Log.e("SanBot", "Błąd otwierania kamery.");
            streamHandle = -1;
        }

        // Przykład: Uruchom grę od razu po podłączeniu (lub podepnij to pod przycisk na ekranie)
        // startCountdownAndPlay();
    }

    // Tę metodę wywołujesz, gdy robot mówi "Trzy!" i chcesz zrobić zdjęcia
    public void startCountdownAndPlay() {
        final List<byte[]> framesToSend = new ArrayList<>();
        final Handler handler = new Handler(Looper.getMainLooper());

        Log.d("SanBot", "Rozpoczynam robienie 3 klatek...");

        Runnable captureTask = new Runnable() {
            int captureCount = 0;

            @Override
            public void run() {
                // 1. Wyciągamy klatkę z kamery SanBota
                Bitmap currentFrame = hdCameraManager.getVideoImage();

                if (currentFrame != null) {
                    // 2. Kompresujemy do JPEG
                    byte[] jpegData = ImageUtils.encodeBitmapToJpeg(currentFrame);
                    framesToSend.add(jpegData);
                } else {
                    Log.w("SanBot", "Pusta klatka!");
                }

                captureCount++;

                if (captureCount < 3) {
                    // Czekamy 100ms i robimy kolejne zdjęcie
                    handler.postDelayed(this, 100);
                } else {
                    // Mamy wszystkie klatki - wysyłamy!
                    sendToPython(framesToSend);
                }
            }
        };

        handler.post(captureTask);
    }

    private void sendToPython(List<byte[]> frames) {
        KlientApi gameClient = new KlientApi();

        gameClient.sendFramesAndPlay(frames, new KlientApi.GameCallback() {
            @Override
            public void onResult(String playerGesture, String robotGesture, String result) {
                // TUTAJ: Logika reakcji robota!
                Log.d("SanBot", "=== WYNIK GRY ===");
                Log.d("SanBot", "Gracz: " + playerGesture);
                Log.d("SanBot", "Robot: " + robotGesture);
                Log.d("SanBot", "Kto wygrał: " + result);

                // Np. odtworzenie animacji / dźwięku na podstawie zmiennej 'result'
            }

            @Override
            public void onError(String errMessage) {
                Log.e("SanBot", "Błąd API: " + errMessage);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // BARDZO WAŻNE: Zamykamy kamerę przed wyjściem, żeby uniknąć wycieku pamięci!
        if (hdCameraManager != null && streamHandle != -1) {
            hdCameraManager.closeStream(streamHandle);
        }
    }
}