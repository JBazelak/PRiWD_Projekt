package com.example.polinav3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.polinav3.gamepad.ButtonInput;
import com.example.polinav3.gamepad.ButtonType;
import com.example.polinav3.gamepad.GameControllerService;
import com.example.polinav3.gamepad.LocalGamepadConnector;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.unit.SpeechManager;

import java.util.ArrayList;

public class MainActivity extends TopBaseActivity {

    private LocalGamepadConnector localGamepadConnector = new LocalGamepadConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<Integer> list = getGameControllerIds();
        Log.d("controllers_amount", String.valueOf(list.size()));
//        TextView v = findViewById(R.id.textStart);
//        v.setText("controllers:"+String.valueOf(list.size()));

        startService(new Intent(this, GameControllerService.class));
    }

    @Override
    protected void onMainServiceConnected() {
        SpeechManager sm = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        if (sm != null) {
//            sm.startSpeak("Dzień dobry dzieciątka!");
        }
        ArrayList<Integer> list = getGameControllerIds();
        Log.d("controllers_amount", String.valueOf(list.size()));
    }

    private void JoystickAction(float x1, float y1, float x2, float y2) {
//        TextView v = findViewById(R.id.textStart);
//        v.setText("X:"+String.valueOf(x)+"|Y:"+String.valueOf(y));
        Log.d("joystick1", "X:"+String.valueOf(x1)+"|Y:"+String.valueOf(y1));
        Log.d("joystick2", "X:"+String.valueOf(x2)+"|Y:"+String.valueOf(y2));
    }
    @Override
    public  boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("test", "button");
//        TextView v = findViewById(R.id.textStart);
//        v.setText("button:"+String.valueOf(event.getKeyCode()));
        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_L1:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.LB,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.RB,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_MODE://Big button in the middle
            case KeyEvent.KEYCODE_BUTTON_B:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.B,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_A:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.A,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_X:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.X,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.Y,0,0));break;
//                Log.d("przycisk", String.valueOf(event.getKeyCode()));

//            default:
//                return super.onKeyDown(event.getKeyCode(), event);
        }
        return super.onKeyDown(event.getKeyCode(), event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }
    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x1 = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);
        float x2=x1;
        if (x1 == 0) {
            x1 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x2 == 0) {
            x2 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y1 = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        float y2=y1;
        if (y1 == 0) {
            y1 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y2 == 0) {
            y2 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }
        JoystickAction(x1,y1,x2,y2);
        // Update the ship object based on the new x and y values
    }

    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public ArrayList<Integer> getGameControllerIds() {
        ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
            }
        }
        return gameControllerDeviceIds;
    }

}