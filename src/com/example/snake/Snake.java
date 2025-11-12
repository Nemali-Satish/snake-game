package com.example.snake;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Snake {
    // Use thread-safe collections for concurrent access
    private final Deque<Point> body = new ConcurrentLinkedDeque<>();
    private Direction curDir = Direction.RIGHT;
    private final Queue<Direction> dirQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean wrap = false;
    private final Object directionLock = new Object(); // For thread-safe direction changes

    public Snake(int startX, int startY) {
        body.clear();
        body.addFirst(new Point(startX, startY));
        // initialize a small body
        body.addLast(new Point(startX-1, startY));
        body.addLast(new Point(startX-2, startY));
    }

    public void queueDirection(Direction d) {
        if (d == null) return;
        
        synchronized (directionLock) {
            // Get the last direction in queue, or current direction if queue is empty
            Direction lastDirection = dirQueue.isEmpty() ? curDir : 
                ((ConcurrentLinkedQueue<Direction>)dirQueue).peek();
                
            // Only queue if not opposite of current direction and not same as last direction
            if (!d.isOpposite(lastDirection) && d != lastDirection) {
                // Limit queue size to prevent input lag
                if (dirQueue.size() < 3) {
                    dirQueue.offer(d);
                } else {
                    // If queue is full, replace the last direction
                    dirQueue.poll();
                    dirQueue.offer(d);
                }
            }
        }
    }

    public void update() {
        synchronized (directionLock) {
            // Process direction changes
            Direction nextDir = dirQueue.poll();
            if (nextDir != null) {
                curDir = nextDir;
            }
            
            // Calculate new head position
            Point head = head();
            if (head == null) return; // Shouldn't happen, but safe check
            
            Point next = new Point(head.x + curDir.dx, head.y + curDir.dy);
            
            // Update body
            body.addFirst(next);
            body.removeLast();
        }
    }

    public void grow(int n) {
        for (int i = 0; i < n; i++) {
            // add a duplicate of the tail
            Point tail = body.peekLast();
            body.addLast(new Point(tail.x, tail.y));
        }
    }

    public void shrink(int n) {
        for (int i = 0; i < n && body.size() > 1; i++) {
            body.removeLast();
        }
    }

    public boolean occupies(Point p) {
        if (p == null) return false;
        return body.parallelStream().anyMatch(b -> p.equals(b));
    }

    public Point head() { return body.peekFirst(); }
    public List<Point> getBody() { return new ArrayList<>(body); }

    public boolean selfCollision() {
        Point h = head();
        if (h == null) return false;
        
        // Skip the head (first element) when checking for collision
        return body.stream()
                  .skip(1)  // Skip the head
                  .anyMatch(segment -> h.equals(segment));
    }

    public void toggleWrap() { 
        // Add a small delay to prevent accidental double-triggering
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        wrap = !wrap; 
    }

    public boolean isWrap() { return wrap; }

    public boolean outOfBounds() {
        Point h = head();
        return h.x < 0 || h.x >= GameConfig.COLS || h.y < 0 || h.y >= GameConfig.ROWS;
    }

    public void wrapPosition(int cols, int rows) {
        Point h = head();
        if (h == null) return;
        
        // Use Math.floorMod for correct wrapping with negative numbers
        h.x = Math.floorMod(h.x, cols);
        h.y = Math.floorMod(h.y, rows);
    }
}
