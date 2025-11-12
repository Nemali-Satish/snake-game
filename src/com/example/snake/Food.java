package com.example.snake;

import java.awt.Point;

public class Food {
    public final Point pos;
    public final boolean isSpecial;

    public Food(int x, int y, boolean special) {
        pos = new Point(x,y);
        isSpecial = special;
    }
}
