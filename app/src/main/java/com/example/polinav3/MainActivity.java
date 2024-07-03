package com.example.polinav3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.polinav3.gamepad.ButtonInput;
import com.example.polinav3.gamepad.ButtonType;
import com.example.polinav3.gamepad.GameControllerService;
import com.example.polinav3.gamepad.LocalGamepadConnector;
import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.ErrorCode;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.ProjectorManager;
import com.sanbot.opensdk.function.unit.SpeechManager;
import com.sanbot.opensdk.function.unit.SystemManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends TopBaseActivity {
    private Button buttonPodazanie;
    private Button buttonRozmowa;
    private Button buttonRozpoznawanie;
    private ImageButton buttonBack;
    private Switch switchProjektor;
    private Switch switchSwiatlo;
    private Switch switchAnimacjaZycia;
    private Switch switchPad;
    private Switch switchPolaczniePC;
    private Switch switchSzwendacz;
    private TextView textViewBatteryStatus;
    private ProgressBar progressBarBattery;
    private ImageView imageViewLogo;

    private ImageView viewCamera;
    private Runnable batteryCheckRunnable;
    private List<Integer> handleList = new ArrayList<>();


    ProjectorManager projectorManager = (ProjectorManager) getUnitManager(FuncConstant.PROJECTOR_MANAGER);
    HardWareManager hardWareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);
    HDCameraManager hdCameraManager = (HDCameraManager) getUnitManager(FuncConstant.HDCAMERA_MANAGER);

    ModularMotionManager modularMotionManager = (ModularMotionManager) getUnitManager(FuncConstant.MODULARMOTION_MANAGER);

    SystemManager systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
    private boolean projectorEnabled = false;
    private long lastSwitchTime = 0;
    private Handler handler = new Handler();
    private int streamId;
    private LocalGamepadConnector localGamepadConnector = new LocalGamepadConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });


        buttonRozmowa = findViewById(R.id.buttonRozmowa);
        buttonRozpoznawanie = findViewById(R.id.buttonRozpoznawanie);
        buttonBack = findViewById(R.id.buttonBack);
        switchProjektor = findViewById(R.id.switchProjektor);
        switchSwiatlo = findViewById(R.id.switchSwiatlo);
        switchAnimacjaZycia = findViewById(R.id.switchAnimacjaZycia);
        switchPad = findViewById(R.id.switchPad);
        switchPolaczniePC = findViewById(R.id.switchPolaczniePC);
        textViewBatteryStatus = findViewById(R.id.textViewBatteryStatus);
        switchSzwendacz = findViewById(R.id.switchSzwendacz);
        imageViewLogo = findViewById(R.id.imageViewLogo);
        viewCamera = findViewById(R.id.viewCamera);

        buttonBack.setOnClickListener(v -> {
            //   closeStream(streamId); // Call method to close stream
            finish(); // Close the application
        });

        startService(new Intent(this, GameControllerService.class));
        localGamepadConnector.addListener(b -> {
            if (GameControllerService.INSTANCE != null) {
                if (b.button == ButtonType.LStick) {
                    GameControllerService.INSTANCE.move(b.ax, -b.ay);
                } else if (b.button == ButtonType.A) {
                    GameControllerService.INSTANCE.stop();
                }
            }
        });

        switchProjektor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OperationResult result = switchProjector(isChecked);
            if (!result.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Please wait 12 seconds before switching again.", Toast.LENGTH_SHORT).show();
                switchProjektor.setOnCheckedChangeListener(null);
                switchProjektor.setChecked(!isChecked);
                switchProjektor.setOnCheckedChangeListener(this::onCheckedChanged);
            }
        });

        switchSwiatlo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OperationResult result = switchWhiteLight(isChecked);
            if (!result.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Failed to switch light.", Toast.LENGTH_SHORT).show();
                switchSwiatlo.setOnCheckedChangeListener(null);
                switchSwiatlo.setChecked(!isChecked);
                switchSwiatlo.setOnCheckedChangeListener(this::onCheckedChanged);
            }
        });
        switchSzwendacz.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OperationResult result = switchWander(isChecked);
            if (!result.isSuccessful()) {
                String errorMessage = getErrorMessage(result.getErrorCode());
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                switchSzwendacz.setChecked(!isChecked); // Revert switch state
            }
        });
        // openStream();
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
        localGamepadConnector.Emit(new ButtonInput(ButtonType.LStick,x1,y1));
        localGamepadConnector.Emit(new ButtonInput(ButtonType.RStick,x2,y2));
    }
    @Override
    public  boolean dispatchKeyEvent(KeyEvent event) {
        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_L1:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.LB,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.RB,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.LT,0,0));break;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                localGamepadConnector.Emit(new ButtonInput(ButtonType.RT,0,0));break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_BUTTON_MODE://Big button in the middle
                break;
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
        float x2=getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Z, historyPos);;
//        float x2=x1;

        if (x1 == 0) {
            x1 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
//        if (x2 == 0) {
//            x2 = getCenteredAxis(event, inputDevice,
//                    MotionEvent.AXIS_Z, historyPos);
//        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y1 = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        float y2=getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_RZ, historyPos);
//        float y2=y1;
        if (y1 == 0) {
            y1 = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
//        if (y2 == 0) {
//            y2 = getCenteredAxis(event, inputDevice,
//                    MotionEvent.AXIS_RZ, historyPos);
//        }
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

    private void startBatteryLevelChecker() {
        batteryCheckRunnable = new Runnable() {
            @Override
            public void run() {
                getBatteryLevel();
                handler.postDelayed(this, 10000); // Sprawdzanie co 10 sekund
            }
        };
        handler.post(batteryCheckRunnable);
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case ErrorCode.FAIL_MOTION_LOCKED:
                return "Cannot respond to this command due to conflict with another operating function.";
            case ErrorCode.FAIL_NO_PERMISSION:
                return "Current user doesn't have permission to open this function.";
            case ErrorCode.FAIL_IS_CHARGE:
                return "Robot cannot move because it’s charging.";
            default:
                return "Unknown error occurred.";
        }
    }

    private void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // This method is used to reset the OnCheckedChangeListener after a failed operation
        if (buttonView.getId() == R.id.switchProjektor) {
            OperationResult result = switchProjector(isChecked);
            if (!result.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Please wait 12 seconds before switching again.", Toast.LENGTH_SHORT).show();
                buttonView.setOnCheckedChangeListener(null);
                buttonView.setChecked(!isChecked);
                buttonView.setOnCheckedChangeListener(this::onCheckedChanged);
            }
        } else if (buttonView.getId() == R.id.switchSwiatlo) {
            OperationResult result = switchWhiteLight(isChecked);
            if (!result.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Failed to switch light.", Toast.LENGTH_SHORT).show();
                buttonView.setOnCheckedChangeListener(null);
                buttonView.setChecked(!isChecked);
                buttonView.setOnCheckedChangeListener(this::onCheckedChanged);
            }
        }
    }

    public OperationResult switchProjector(boolean isOpen) {
        long currentTime = System.currentTimeMillis();
        if (isOpen) {
            projectorEnabled = true;
            lastSwitchTime = currentTime;
            projectorManager.switchProjector(true);

            // Zablokuj możliwość wyłączenia projektora przez 12 sekund
            handler.postDelayed(() -> {
                projectorEnabled = false; // Po 12 sekundach można ponownie wyłączyć projektora
            }, 12000); // 12 sekund
        } else {
            if (currentTime - lastSwitchTime < 12000) {
                return new OperationResult(false); // Nie pozwalaj na wyłączenie przed upływem 12 sekund
            }
            projectorManager.switchProjector(false);
        }
        return new OperationResult(true);
    }

    public OperationResult switchWhiteLight(boolean isOpen) {
        if (isOpen) {
            hardWareManager.switchWhiteLight(true);
        } else {
            hardWareManager.switchWhiteLight(false);
        }
        return new OperationResult(true); // Na razie symulujemy, że operacja zawsze jest udana
    }

    private OperationResult switchWander(boolean isOpen) {
        // Call the switchWander method on the ModularMotionManager
        if (isOpen) {
            modularMotionManager.switchWander(true);
        } else {
            modularMotionManager.switchWander(false);
        }
        return new OperationResult(true);
    }

    public OperationResult openStream(StreamOption streamOption) {
        streamOption.setChannel(StreamOption.MAIN_STREAM);
        streamOption.setDecodType(StreamOption.HARDWARE_DECODE);
        streamOption.setJustIframe(false);

        // Wywołanie metody openStream z hdCameraManager
        hdCameraManager.openStream(streamOption);
        return new OperationResult(true);
    }

    public OperationResult closeStream(int handle) {
        hdCameraManager.closeStream(handle);
        return new OperationResult(true);
    }
    public interface MediaListener {
        void getVideoStream(byte[] data);
        void getAudioStream(byte[] data);
    }

    // Define OperationResult class
    public static class OperationResult {
        private boolean successful;
        private int streamId;
        private int errorCode;

        public OperationResult(boolean successful) {
            this.successful = successful;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public int getStreamId() {
            return streamId;
        }

        public void setStreamId(int streamId) {
            this.streamId = streamId;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    private int getBatteryLevel() {
        // Uzyskaj poziom baterii
        int batteryLevel = systemManager.getBatteryValue();
        runOnUiThread(() -> {
            textViewBatteryStatus.setText("Battery Level: " + batteryLevel + "%");
        });
        Log.d("BatteryStatus", "Current battery level: " + batteryLevel + "%");
        // Jeśli uzyskano poziom baterii, zwróć wartość
        // W rzeczywistym zastosowaniu powinieneś obsłużyć różne przypadki błędów, jak np. brak dostępu do danych o baterii.
        return batteryLevel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Zwolnij zasoby Handlera i przerwij cykliczne sprawdzanie poziomu baterii
        if (handler != null && batteryCheckRunnable != null) {
            handler.removeCallbacks(batteryCheckRunnable);
        }
    }
}