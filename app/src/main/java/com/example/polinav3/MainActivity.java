package com.example.polinav3;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sanbot.opensdk.base.TopBaseActivity;
import com.sanbot.opensdk.beans.ErrorCode;
import com.sanbot.opensdk.beans.FuncConstant;
import com.sanbot.opensdk.function.beans.StreamOption;
import com.sanbot.opensdk.function.unit.HDCameraManager;
import com.sanbot.opensdk.function.unit.HardWareManager;
import com.sanbot.opensdk.function.unit.ModularMotionManager;
import com.sanbot.opensdk.function.unit.ProjectorManager;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register(MainActivity.class);
        setContentView(R.layout.activity_main);

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
        startBatteryLevelChecker();


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
    protected void onMainServiceConnected() {
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
