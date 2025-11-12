package com.example.snake;

import javax.swing.Timer;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;      
import java.util.Random;       


public class GamePanel extends JPanel implements ActionListener {
    private Snake snake;
    private Food food;
    private final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<>());
    private PowerUp activePower = null;
    private final Random rnd = new Random();
    private Timer timer;
    private Timer powerUpTimer;
    private boolean running = false;
    private boolean paused = false;
    private int score = 0;
    private int level = 1;
    private int tickMs = GameConfig.BASE_TICK_MS;
    private final HighScoreStore hsStore = new HighScoreStore();
    private int highScore = hsStore.load();

    public GamePanel() {
        try {
            setPreferredSize(GameConfig.PREF_SIZE);
            setBackground(Color.BLACK);
            setFocusable(true);
            initGame();
            setupInput();
            startGame();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to initialize game: " + e.getMessage(),
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initGame() {
        // Clean up existing timers
        cleanupTimers();
        
        // Initialize game state
        try {
            highScore = Math.max(0, hsStore.load()); // Ensure non-negative score
        } catch (Exception e) {
            highScore = 0;
            System.err.println("Failed to load high score: " + e.getMessage());
        }
        snake = new Snake(GameConfig.COLS/2, GameConfig.ROWS/2);
        spawnFood();
        spawnObstacles(GameConfig.INITIAL_OBSTACLES);
        score = 0;
        level = 1;
        tickMs = GameConfig.BASE_TICK_MS;
        running = true;
        paused = false;
    }

    private void startGame() {
        timer = new Timer(tickMs, this);
        timer.start();
    }

    private void restartGame() {
        timer.stop();
        initGame();
        timer.setDelay(tickMs);
        timer.start();
    }

    private void setupInput() {
        // Key bindings rather than KeyListener for more reliable key-handling
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("UP"), "up");
        im.put(KeyStroke.getKeyStroke("DOWN"), "down");
        im.put(KeyStroke.getKeyStroke("LEFT"), "left");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        im.put(KeyStroke.getKeyStroke("P"), "pause");
        im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-wrap");

        am.put("up", new DirectionAction(Direction.UP));
        am.put("down", new DirectionAction(Direction.DOWN));
        am.put("left", new DirectionAction(Direction.LEFT));
        am.put("right", new DirectionAction(Direction.RIGHT));
        am.put("pause", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
        am.put("toggle-wrap", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                snake.toggleWrap();
            }
        });
    }

    private class DirectionAction extends AbstractAction {
        private final Direction dir;
        DirectionAction(Direction dir){ this.dir = dir; }
        @Override public void actionPerformed(ActionEvent e){
            snake.queueDirection(dir);
        }
    }

    private void spawnFood() {
        Point p;
        do {
            p = new Point(rnd.nextInt(GameConfig.COLS), rnd.nextInt(GameConfig.ROWS));
        } while (snake.occupies(p) || obstaclesContains(p));
        boolean isSpecial = rnd.nextDouble() < 0.12; // 12% special
        food = new Food(p.x, p.y, isSpecial);
    }

    private boolean obstaclesContains(Point p) {
        if (p == null) return false;
        // Use parallel stream for better performance with many obstacles
        return obstacles.parallelStream()
                      .anyMatch(obs -> obs != null && p.equals(obs.pos));
    }

    private void spawnObstacles(int n) {
        obstacles.clear();
        for (int i = 0; i < n; i++) {
            Point p;
            do {
                p = new Point(rnd.nextInt(GameConfig.COLS), rnd.nextInt(GameConfig.ROWS));
            } while (snake.occupies(p));
            obstacles.add(new Obstacle(p.x, p.y));
        }
    }

    private void maybeSpawnPowerUp() {
        if (activePower != null || food == null) return;
        if (rnd.nextDouble() < GameConfig.POWER_UP_SPAWN_CHANCE) {
            Point p;
            int attempts = 0;
            final int maxAttempts = 50; // Prevent infinite loops
            
            do {
                if (attempts++ >= maxAttempts) return; // Give up if we can't find a valid spot
                p = new Point(rnd.nextInt(GameConfig.COLS), rnd.nextInt(GameConfig.ROWS));
            } while (snake.occupies(p) || obstaclesContains(p) || p.equals(food.pos));
            
            activePower = new PowerUp(p.x, p.y, PowerUp.Type.randomType());
            
            // Auto-remove power-up after some time if not collected
            if (powerUpTimer != null) {
                powerUpTimer.stop();
            }
            powerUpTimer = new Timer(GameConfig.POWER_UP_DURATION, e -> {
                if (activePower != null) {
                    activePower = null;
                    repaint();
                }
            });
            powerUpTimer.setRepeats(false);
            powerUpTimer.start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running || paused) return;

        // move snake
        snake.update();

        // handle wrap vs wall collision
        if (!snake.isWrap()) {
            if (snake.outOfBounds()) {
                gameOver();
                return;
            }
        } else {
            snake.wrapPosition(GameConfig.COLS, GameConfig.ROWS);
        }

        // obstacle collision
        for (Obstacle o: obstacles) {
            if (snake.head().equals(o.pos)) {
                gameOver();
                return;
            }
        }

        // self-collision
        if (snake.selfCollision()) {
            gameOver();
            return;
        }

        // food eaten
        if (snake.head().equals(food.pos)) {
            snake.grow(food.isSpecial ? 3 : 1); // special gives extra growth
            score += food.isSpecial ? 5 : 1;
            maybeLevelUp();
            spawnFood();
        }

        // power-up pickup
        if (activePower != null && snake.head().equals(activePower.pos)) {
            applyPowerUp(activePower);
            activePower = null;
        }

        // occasionally spawn power-up
        maybeSpawnPowerUp();

        // move timer speed for speed boost expiry handled in power-up state
        timer.setDelay(tickMs);

        repaint();
    }

    private void applyPowerUp(PowerUp p) {
        if (p == null) return;
        
        switch (p.type) {
            case SPEED_BOOST:
                int originalSpeed = tickMs;
                tickMs = Math.max(GameConfig.MIN_TICK_MS, tickMs - GameConfig.SPEED_BOOST_AMOUNT);
                // schedule restoring speed after duration
                if (powerUpTimer != null) {
                    powerUpTimer.stop();
                }
                powerUpTimer = new Timer(GameConfig.SPEED_BOOST_DURATION, ev -> {
                    tickMs = Math.min(GameConfig.BASE_TICK_MS, originalSpeed);
                    powerUpTimer = null;
                });
                powerUpTimer.setRepeats(false);
                powerUpTimer.start();
                break;
                
            case SHRINK:
                snake.shrink(GameConfig.SHRINK_AMOUNT);
                break;
                
            case CLEAR_OBSTACLES:
                obstacles.clear();
                // Add some score for clearing obstacles
                score += obstacles.size();
                break;
        }
    }

    private void maybeLevelUp() {
        int newLevel = 1 + score / GameConfig.LEVEL_UP_SCORE;
        if (newLevel > level) {
            level = newLevel;
            tickMs = Math.max(20, GameConfig.BASE_TICK_MS - (level - 1) * GameConfig.SPEEDUP_STEP);
            // add an obstacle each level
            spawnObstacles(Math.min(30, obstacles.size() + 1));
        }
    }

    private void togglePause() {
        paused = !paused;
        repaint();
    }

    private void cleanupTimers() {
        if (powerUpTimer != null) {
            powerUpTimer.stop();
            powerUpTimer = null;
        }
        if (timer != null) {
            timer.stop();
            // Don't set timer to null here as it's used in restart
        }
    }
    
    public void dispose() {
        cleanupTimers();
        try {
            hsStore.close();
        } catch (Exception e) {
            System.err.println("Error closing high score store: " + e.getMessage());
        }
    }
    
    private void gameOver() {
        running = false;
        cleanupTimers();
        if (score > highScore) {
            highScore = score;
            hsStore.save(highScore);
        }
        
        // Show game over dialog in the Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            int res = JOptionPane.showOptionDialog(this,
                String.format("Game over. Score: %d%nHigh Score: %d%nRestart?", score, highScore),
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null, null, null);
                
            if (res == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Use Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();

        // draw grid (optional faint)
        g2.setColor(new Color(30, 30, 30));
        for (int x = 0; x <= GameConfig.COLS; x++) {
            g2.drawLine(x * GameConfig.TILE_SIZE, 0, x * GameConfig.TILE_SIZE, getHeight());
        }
        for (int y = 0; y <= GameConfig.ROWS; y++) {
            g2.drawLine(0, y * GameConfig.TILE_SIZE, getWidth(), y * GameConfig.TILE_SIZE);
        }

        // draw food
        drawTile(g2, food.pos.x, food.pos.y, food.isSpecial ? Color.MAGENTA : Color.GREEN);

        // draw obstacles
        for (Obstacle o : obstacles) {
            drawTile(g2, o.pos.x, o.pos.y, Color.DARK_GRAY);
        }

        // draw powerup
        if (activePower != null) {
            drawTile(g2, activePower.pos.x, activePower.pos.y, Color.ORANGE);
        }

        // draw snake
        drawSnake(g2);

        // HUD
        g2.setColor(Color.WHITE);
        g2.drawString("Score: " + score, 10, 14);
        g2.drawString("High: " + highScore, 100, 14);
        g2.drawString("Level: " + level, 180, 14);
        g2.drawString("Wrap: " + (snake.isWrap() ? "ON (Space)" : "OFF (Space)"), 260, 14);
        g2.drawString(paused ? "PAUSED (P)" : "", 420, 14);

        g2.dispose();
    }

    private void drawTile(Graphics2D g2, int col, int row, Color c) {
        int x = col * GameConfig.TILE_SIZE;
        int y = row * GameConfig.TILE_SIZE;
        g2.setColor(c);
        g2.fillRoundRect(x + 2, y + 2, GameConfig.TILE_SIZE - 4, GameConfig.TILE_SIZE - 4, 6, 6);
    }

    private void drawSnake(Graphics2D g2) {
        // Head
        g2.setColor(Color.YELLOW);
        Point head = snake.head();
        drawTile(g2, head.x, head.y, Color.YELLOW);

        // body gradient
        List<Point> pts = snake.getBody();
        for (int i = 1; i < pts.size(); i++) {
            float t = (float) i / Math.max(1, pts.size());
            Color c = new Color( (int)(0 + t*200), (int)(100 + (1-t)*100), 40 );
            drawTile(g2, pts.get(i).x, pts.get(i).y, c);
        }
    }
}
