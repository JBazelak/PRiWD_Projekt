package com.example.polinav3.gamepad;

public class ButtonInput {
    public ButtonInput(ButtonType button, float ax, float ay) {
        this.button = button;
        this.ax = ax;
        this.ay = ay;
    }

    public final ButtonType button;
    public final float ax;
    public final float ay;
}
