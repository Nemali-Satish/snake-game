package com.example.snake;

import java.awt.Dimension;

public final class GameConfig {
    private GameConfig() {}

    // Game board configuration
    public static final int TILE_SIZE = 24;            // pixels
    public static final int COLS = 28;
    public static final int ROWS = 24;
    public static final Dimension PREF_SIZE = new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE);
    
    // Game timing and speed
    public static final int BASE_TICK_MS = 100;        // base tick (decrease for faster)
    public static final int MIN_TICK_MS = 20;          // minimum tick time (maximum speed)
    public static final int SPEEDUP_STEP = 5;          // ms decrease per level
    
    // Game progression
    public static final int LEVEL_UP_SCORE = 5;
    public static final int INITIAL_OBSTACLES = 6;     // Starting number of obstacles
    
    // Power-up configuration
    public static final double POWER_UP_SPAWN_CHANCE = 0.05;  // 5% chance per tick
    public static final int POWER_UP_DURATION = 5000;         // 5 seconds
    public static final int SPEED_BOOST_AMOUNT = 40;          // How much to reduce tick time by
    public static final int SPEED_BOOST_DURATION = 8000;      // 8 seconds
    public static final int SHRINK_AMOUNT = 3;                // Number of segments to shrink by
    
    // File paths
    public static final String HIGH_SCORE_FILE = System.getProperty("user.home") + "/.advanced_snake_highscore";
}
