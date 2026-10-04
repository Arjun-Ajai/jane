package com.jane.flappy;

import java.awt.Color;
import java.awt.Graphics2D;

public class Pipe {
    private double x;
    private double gapCenterY;
    private java.util.Set<com.jane.env.Agent> passedBy = new java.util.HashSet<>();

    public Pipe(double x, java.util.Random random) {
        this.x = x;
        // Keep the larger training gap away from the screen edges.
        this.gapCenterY = FlappyConfig.WINDOW_HEIGHT * 0.25
                + random.nextDouble() * (FlappyConfig.WINDOW_HEIGHT * 0.5);
    }

    public void update() {
        x -= FlappyConfig.PIPE_SPEED;
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(0, 180, 0));
        
        // Top pipe
        int topHeight = (int) (gapCenterY - FlappyConfig.PIPE_GAP / 2);
        g.fillRect((int) x, 0, FlappyConfig.PIPE_WIDTH, topHeight);
        
        // Bottom pipe
        int bottomY = (int) (gapCenterY + FlappyConfig.PIPE_GAP / 2);
        int bottomHeight = FlappyConfig.WINDOW_HEIGHT - bottomY;
        g.fillRect((int) x, bottomY, FlappyConfig.PIPE_WIDTH, bottomHeight);
    }
    
    public boolean collides(Bird bird) {
        double bx = bird.getX();
        double by = bird.getY();
        double r = FlappyConfig.BIRD_RADIUS;
        
        // Check if bird is within horizontal pipe bounds
        if (bx + r > x && bx - r < x + FlappyConfig.PIPE_WIDTH) {
            // Check vertical collision
            int topY = (int) (gapCenterY - FlappyConfig.PIPE_GAP / 2);
            int bottomY = (int) (gapCenterY + FlappyConfig.PIPE_GAP / 2);
            
            if (by - r < topY || by + r > bottomY) {
                return true;
            }
        }
        return false;
    }

    public boolean isOffscreen() {
        return x + FlappyConfig.PIPE_WIDTH < 0;
    }

    public double getX() { return x; }
    public double getGapCenterY() { return gapCenterY; }
    public boolean isPassedBy(com.jane.env.Agent agent) { return passedBy.contains(agent); }
    public void setPassedBy(com.jane.env.Agent agent) { passedBy.add(agent); }
}
