package com.example.polinav3;

public enum Buttons {

    LStick(1), RStick(2), X(3), Y(4), A(5), B(6), LT(7), RT(8), LB(9), RB(10);
    Buttons(int id) {
        this.id = id;
    }

    public final int id;
}
