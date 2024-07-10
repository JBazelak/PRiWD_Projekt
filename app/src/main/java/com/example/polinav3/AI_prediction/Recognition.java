package com.example.polinav3.AI_prediction;

import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.example.polinav3.R;
import com.example.polinav3.video.CameraHandlerThread;
import com.example.polinav3.video.MediaStreamManager;
import com.example.polinav3.video.VisionMediaDecoder;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.unit.HDCameraManager;


public class Recognition extends TopBaseActivity implements SurfaceHolder.Callback {

    private HDCameraManager hdCameraManager;
    private VisionMediaDecoder mediaDecoder;
    private MediaStreamManager mediaStreamManager;
    private SurfaceView sv;
    private ImageButton returnButton;
    private CameraHandlerThread cameraTread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(Recognition.class);
        setContentView(R.layout.activity_recognition);
        cameraTread = new CameraHandlerThread();
        hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
        mediaDecoder = new VisionMediaDecoder();
        mediaStreamManager = new MediaStreamManager(hdCameraManager, mediaDecoder);
        sv= findViewById(R.id.surfaceView);
        sv.getHolder().addCallback(this);
        mediaStreamManager.openStream(sv.getHolder().getSurface());
        Log.i("Camera-AI", "Stworzono nową kamera view" );
        returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(v -> {
            finish();
        });

    }

    @Override
    protected void onMainServiceConnected() {

    }

    private void startCamera(Surface surface) {
        cameraTread.postTask(() -> mediaStreamManager.openStream(surface));
    }


    private void closeCamera (Surface surface){
        cameraTread.postTask(() -> mediaStreamManager.closeStream(surface));
    }


    @Override
    protected void onDestroy() {
        mediaStreamManager.closeStream(sv.getHolder().getSurface());
        super.onDestroy();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startCamera(holder.getSurface());
        //mediaStreamManager.openStream(holder.getSurface());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mediaStreamManager.changeSurface(holder.getSurface(), format, width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        closeCamera(holder.getSurface());
        //mediaStreamManager.closeStream(holder.getSurface());
    }
}