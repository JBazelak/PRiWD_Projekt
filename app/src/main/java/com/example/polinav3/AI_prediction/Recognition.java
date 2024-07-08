package com.example.polinav3.AI_prediction;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.polinav3.R;
import com.example.polinav3.video.VisionMediaDecoder;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.beans.OperationResult;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.interfaces.media.MediaStreamListener;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Recognition extends TopBaseActivity implements SurfaceHolder.Callback {

    HDCameraManager hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);
    private int mHeight, mWidth;
    private VisionMediaDecoder mediaDecoder = new VisionMediaDecoder();
    //private ImageView imageFromCamera;
    private SurfaceView sv;
    private List<Integer> handleList = new ArrayList<>();
    static String TAG_camera = "CAMERA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(Recognition.class);
        setContentView(R.layout.activity_recognition);
        sv= findViewById(R.id.surfaceView);
        //imageFromCamera = findViewById(R.id.imageFromCamera);
        initHardwareListeners();
        sv.getHolder().addCallback(this);
    }

    @Override
    protected void onMainServiceConnected() {

    }

    private void initHardwareListeners(){
        hdCameraManager.setMediaListener(new MediaStreamListener() {
            @Override
            public void getVideoStream(int handle, byte[] bytes, int width, int height) {
                if (mediaDecoder != null) {
                    if (width != mWidth || height != mHeight) {
                        mediaDecoder.onCreateCodec(width, height);
                        mWidth = width;
                        mHeight = height;
                    }
                    mediaDecoder.drawVideoSample(ByteBuffer.wrap(bytes));
                    Log.i(TAG_camera,"getVideoStream: Video data:" + bytes.length);
                }
            }

            @Override
            public void getAudioStream(int i, @NonNull byte[] bytes) {

            }
        }
        );
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Set parameters and open the media stream for video
        StreamOption streamOption = new StreamOption();
        streamOption.setChannel(StreamOption.MAIN_STREAM);
        streamOption.setDecodType(StreamOption.HARDWARE_DECODE);
        streamOption.setJustIframe(false);
        OperationResult operationResult = hdCameraManager.openStream(streamOption);
        Log.i(TAG_camera, "surfaceCreated: operationResult=" + operationResult.getResult());
        int result = Integer.valueOf(operationResult.getResult());
        if (result != -1) {
            handleList.add(result);
        }
        mediaDecoder.setSurface(holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG_camera, "surfaceDestroyed: ");
        //Close media stream
        if (handleList.size() > 0) {
            for (int handle : handleList) {
                Log.i(TAG_camera, "surfaceDestroyed: " + hdCameraManager.closeStream(handle));
            }
        }
        handleList = null;
        mediaDecoder.stopDecoding();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}