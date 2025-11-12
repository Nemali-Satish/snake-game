package com.example.snake;

import java.awt.Point;
import java.util.Random;

public class PowerUp {
    public enum Type { SPEED_BOOST, SHRINK, CLEAR_OBSTACLES;
        private static final Type[] vals = values();
        public static Type randomType() { return vals[new Random().nextInt(vals.length)]; }
    }
    public final Point pos;
    public final Type type;

    public PowerUp(int x, int y, Type type) { pos = new Point(x,y); this.type = type; }
}
