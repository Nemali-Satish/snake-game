package com.example.snake;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe high score storage with file persistence.
 */
public class HighScoreStore implements AutoCloseable {
    private final ReentrantLock lock = new ReentrantLock();
    private boolean closed = false;

    /**
     * Loads the high score from the file.
     * @return The high score, or 0 if no score exists or an error occurs.
     */
    public int load() {
        checkNotClosed();
        lock.lock();
        try {
            Path path = Paths.get(GameConfig.HIGH_SCORE_FILE);
            if (!Files.exists(path)) {
                return 0;
            }
            
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line = reader.readLine();
                if (line == null) {
                    return 0;
                }
                return Math.max(0, Integer.parseInt(line.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Invalid high score format: " + e.getMessage());
                return 0;
            } catch (IOException e) {
                System.err.println("Error reading high score: " + e.getMessage());
                return 0;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Saves the high score to the file.
     * @param score The score to save (must be non-negative).
     * @throws IllegalArgumentException if score is negative.
     */
    public void save(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        
        checkNotClosed();
        lock.lock();
        try {
            Path path = Paths.get(GameConfig.HIGH_SCORE_FILE);
            Path parent = path.getParent();
            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    System.err.println("Failed to create parent directories: " + e.getMessage());
                }
            }
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(Integer.toString(score));
            } catch (IOException e) {
                System.err.println("Failed to save high score: " + e.getMessage());
                throw new RuntimeException("Failed to save high score", e);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            // Any additional cleanup if needed
        }
    }
    
    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("HighScoreStore has been closed");
        }
    }
}
