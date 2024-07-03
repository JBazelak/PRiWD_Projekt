package com.example.polinav3.gamepad;

import java.util.ArrayList;
import java.util.List;

public class LocalGamepadConnector implements ButtonConnector{

    private List<ButtonListener> buttonListeners = new ArrayList<>();

    @Override
    public void addListener(ButtonListener listener) {
        buttonListeners.add(listener);
    }
    public void Emit(ButtonInput input){
        for (ButtonListener button : buttonListeners) {
            button.onButtonPressed(input);
        }
    }
}
