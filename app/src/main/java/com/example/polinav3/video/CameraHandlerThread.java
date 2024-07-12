package com.example.polinav3.video;

import android.os.Handler;
import android.os.Looper;


public class CameraHandlerThread extends Thread {
    private Handler handler;

    public CameraHandlerThread() {
        start();
        while (handler == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        Looper.prepare();
        handler = new Handler();
        Looper.loop();
    }

    public void postTask(Runnable task) {
        handler.post(task);
    }
}