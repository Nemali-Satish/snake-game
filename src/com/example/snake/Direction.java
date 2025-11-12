package com.example.snake;

public enum Direction {
    UP(0,-1), DOWN(0,1), LEFT(-1,0), RIGHT(1,0);

    public final int dx, dy;
    Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }

    public boolean isOpposite(Direction other) {
        return (this.dx + other.dx == 0 && this.dy + other.dy == 0);
    }
}
