package com.example.polinav3;

public class ButtonInput {
    public ButtonInput(Buttons button, float axis, boolean pressed) {
        this.button = button;
        this.axis = axis;
        this.pressed = pressed;
    }

    public final Buttons button;
    public final float axis;
    public final boolean pressed;
}
