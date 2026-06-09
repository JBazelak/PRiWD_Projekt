package com.example.polinav3_SGB;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.beans.FaceRecognizeBean;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.MediaManager;
import com.sanbot.opensdk.function.unit.interfaces.media.FaceRecognizeListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends TopBaseActivity{

    List<byte[]> images = new ArrayList<>();
//    List<Bitmap> snapshots = new ArrayList<>();
    Button playGameButton = findViewById(R.id.playGameButton);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private HDCameraManager hdCameraManager;
    private int streamHandle = -1;

    ByteArrayOutputStream stream = new ByteArrayOutputStream();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_game);

        playGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i<4; i++){
                            try {
                                images.add(captureSingleFrame());
                                executor.wait(50);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });


            }
        });
    }

    @Override
    protected void onMainServiceConnected() {
        hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
        StreamOption streamOption = new StreamOption();
        streamOption.setChannel(StreamOption.MAIN_STREAM); // Główny strumień HD (1280*720) [cite: 1073]
        streamOption.setDecodType(StreamOption.HARDWARE_DECODE); // Użycie dekodowania sprzętowego [cite: 1068]
        streamOption.setJustIframe(false); // Pozwala pobierać wszystkie klatki, a nie tylko klatki kluczowe I-frame [cite: 1070]

        // Żądanie otwarcia strumienia [cite: 1061, 1063]
        OperationResult operationResult = hdCameraManager.openStream(streamOption);

        // Zapisanie uchwytu (handle) strumienia [cite: 1086, 1087, 1088]
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
            // Złapanie klatki z otwartego strumienia [cite: 1129, 1131]
            Bitmap frame = hdCameraManager.getVideoImage();
            frame.compress(Bitmap.CompressFormat.JPEG, 70, stream);

            if (frame != null) {
                return stream.toByteArray(); // Mamy poprawne zdjęcie dłoni!
            }
        }
        return null; // Nie udało się pobrać klatki
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Bezpieczne zamknięcie strumienia wideo przed zniszczeniem Activity
        if (hdCameraManager != null && streamHandle != -1) {
            hdCameraManager.closeStream(streamHandle);
        }
    }

}


