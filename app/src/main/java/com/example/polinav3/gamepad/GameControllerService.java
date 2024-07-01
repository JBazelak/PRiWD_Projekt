package com.example.polinav3.gamepad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.sanbot.opensdk.base.BindBaseService;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.wheelmotion.NoAngleWheelMotion;
import com.sanbot.opensdk.function.beans.wheelmotion.RelativeAngleWheelMotion;
import com.sanbot.opensdk.function.unit.HandMotionManager;
import com.sanbot.opensdk.function.unit.WheelMotionManager;
import com.sanbot.opensdk.function.unit.WingMotionManager;

public class GameControllerService extends BindBaseService {
    private WheelMotionManager wheels;
    private WingMotionManager wings;
    private float x, y;
    private boolean running = true;
    public GameControllerService() {
        Log.d("test", "creating service");
    }
    public static GameControllerService INSTANCE = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        register(GameControllerService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onMainServiceConnected() {
        Log.d("test", "connecting service");
        wheels = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);
        wings = (WingMotionManager) getUnitManager(FuncConstant.WINGMOTION_MANAGER);
        INSTANCE = this;

        new Thread(this::roboLoop).start();
    }

    private void roboLoop() {
        while (running) {
            if (this.y != 0 || this.x != 0) {
                byte action = xyToAction(x, y);
                Log.d("action", String.valueOf(action));
                wheels.doNoAngleMotion(new NoAngleWheelMotion(action, 3, 125));
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private byte xyToAction(float x, float y) {
        Log.d("move", "x: " + x + "; y: " + y);
        if (y > 0.4) {
            if (x > 0.4) {
                Log.d("move", "right forward");
                return NoAngleWheelMotion.ACTION_RIGHT_FORWARD;
            } else if (x < -0.4) {

                Log.d("move", "left forward");

                return NoAngleWheelMotion.ACTION_LEFT_FORWARD;
            }
            Log.d("move", " forward");

            return NoAngleWheelMotion.ACTION_FORWARD;
        } else if (y < -0.4) {
            if (x > 0.4) {
                Log.d("move", "right back");

                return NoAngleWheelMotion.ACTION_RIGHT_BACK;
            } else if (x < -0.4) {
                Log.d("move", "left back");

                return NoAngleWheelMotion.ACTION_LEFT_BACK;
            }
            Log.d("move", " back");

            return NoAngleWheelMotion.ACTION_BACK;
        } else if (x > 0.4) {
            return NoAngleWheelMotion.ACTION_RIGHT;
        } else if (x < -0.4) {
            return NoAngleWheelMotion.ACTION_LEFT;
        }
        return NoAngleWheelMotion.ACTION_STOP;
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void stop() {
        wheels.doNoAngleMotion(new NoAngleWheelMotion(NoAngleWheelMotion.ACTION_STOP, 0));
    }
}