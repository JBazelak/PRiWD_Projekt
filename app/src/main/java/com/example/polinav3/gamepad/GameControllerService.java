package com.example.polinav3.gamepad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sanbot.opensdk.base.BindBaseService;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.unit.HandMotionManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;

public class GameControllerService extends BindBaseService {
    private WheelMotionManager wheels;
    private WingMotionManager wings;
    public GameControllerService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onMainServiceConnected() {
        wheels = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);
        wings = (WingMotionManager) getUnitManager(FuncConstant.WINGMOTION_MANAGER);
    }
}