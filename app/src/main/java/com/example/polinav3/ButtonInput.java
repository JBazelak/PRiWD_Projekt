package com.example.polinav3;

public class ButtonInput {
    public ButtonInput(ButtonType button, float axis, boolean pressed) {
        this.button = button;
        this.axis = axis;
        this.pressed = pressed;
    }

    public final ButtonType button;
    public final float axis;
    public final boolean pressed;
}
