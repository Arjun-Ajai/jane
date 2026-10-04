package com.jane.flappy;

import com.jane.core.NeuralNetwork;
import com.jane.env.Agent;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bird extends Agent {
    private static final Image SPRITE = loadSprite();
    private double y;
    private double velocity;
    private int framesSurvived = 0;
    private int pipesPassed = 0;

    public Bird(NeuralNetwork brain) {
        super(brain);
        this.y = FlappyConfig.WINDOW_HEIGHT / 2.0;
        this.velocity = 0;
    }

    @Override
    public void update() {
        if (!alive) return;
        
        velocity += FlappyConfig.GRAVITY;
        if (velocity > FlappyConfig.MAX_VELOCITY) {
            velocity = FlappyConfig.MAX_VELOCITY;
        }
        
        y += velocity;
        
        // Ground or ceiling collision
        if (y > FlappyConfig.WINDOW_HEIGHT - FlappyConfig.BIRD_RADIUS || y < FlappyConfig.BIRD_RADIUS) {
            alive = false;
        }
    }
    
    public void flap() {
        if (alive) {
            velocity = FlappyConfig.FLAP_STRENGTH;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!alive) return; // or draw greyed out if preferred

        int width = FlappyConfig.BIRD_WIDTH;
        int height = FlappyConfig.BIRD_HEIGHT;
        g.drawImage(SPRITE, FlappyConfig.BIRD_X - width / 2, (int) y - height / 2,
                width, height, null);
    }

    private static Image loadSprite() {
        try {
            Image sprite = ImageIO.read(Bird.class.getResource("/images/flappy-bird.png"));
            if (sprite == null) {
                throw new IllegalStateException("Bird sprite resource is empty");
            }
            return sprite;
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Unable to load bird sprite", e);
        }
    }

    public double getY() { return y; }
    public double getVelocity() { return velocity; }
    public double getX() { return FlappyConfig.BIRD_X; }
    public int getFramesSurvived() { return framesSurvived; }
    public void incrementFramesSurvived() { this.framesSurvived++; }
    public int getPipesPassed() { return pipesPassed; }
    public void incrementPipesPassed() { this.pipesPassed++; }
}
