package com.jane.flappy;

import com.jane.env.Agent;
import com.jane.env.Environment;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class FlappyBirdEnvironment implements Environment {
    private List<Pipe> pipes;
    private int ticks;
    private java.util.Random pipeRandom;

    public FlappyBirdEnvironment() {
        reset(System.nanoTime());
    }

    public void reset(long seed) {
        pipeRandom = new java.util.Random(seed);
        pipes = new ArrayList<>();
        pipes.add(new Pipe(FlappyConfig.PIPE_SPAWN_X, pipeRandom));
        ticks = 0;
    }

    @Override
    public void reset() {
        reset(System.nanoTime());
    }

    public void update() {
        ticks++;

        // Spawn new pipes
        if (ticks % (FlappyConfig.PIPE_SPACING / FlappyConfig.PIPE_SPEED) == 0) {
            pipes.add(new Pipe(FlappyConfig.PIPE_SPAWN_X, pipeRandom));
        }

        // Update pipes
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe p = pipes.get(i);
            p.update();
            
            if (p.isOffscreen()) {
                pipes.remove(i);
            }
        }
    }

    public void render(Graphics2D g) {
        for (Pipe p : pipes) {
            p.render(g);
        }
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    public double[] getState(Agent agent) {
        Bird bird = (Bird) agent;
        Pipe nextPipe = getNextPipe(bird);
        
        if (nextPipe == null) {
            return new double[]{0, 0, 0, 0};
        }

        double[] state = new double[4];
        // birdY normalized
        state[0] = clamp(bird.getY() / FlappyConfig.WINDOW_HEIGHT, 0, 1);
        // birdVelocity normalized
        state[1] = clamp((bird.getVelocity() + FlappyConfig.MAX_VELOCITY) / (2 * FlappyConfig.MAX_VELOCITY), 0, 1);
        // distanceToNextPipe normalized
        state[2] = clamp(nextPipe.getX() / FlappyConfig.WINDOW_WIDTH, 0, 1);
        // nextPipeGapCenterY normalized
        state[3] = clamp(nextPipe.getGapCenterY() / FlappyConfig.WINDOW_HEIGHT, 0, 1);

        return state;
    }

    private Pipe getNextPipe(Bird bird) {
        for (Pipe p : pipes) {
            if (p.getX() + FlappyConfig.PIPE_WIDTH > bird.getX()) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void applyAction(Agent agent, double[] action) {
        Bird bird = (Bird) agent;
        if (action.length > 0 && action[0] > 0.5) {
            bird.flap();
        }
    }

    @Override
    public double getFitness(Agent agent) {
        Bird bird = (Bird) agent;
        double frameScore = bird.getFramesSurvived();
        double pipeScore = bird.getPipesPassed() * 1000.0;
        
        Pipe nextPipe = getNextPipe(bird);
        double centeringBonus = 0;
        if (nextPipe != null) {
            centeringBonus = 100.0 - Math.min(100.0, Math.abs(bird.getY() - nextPipe.getGapCenterY()));
        }
        
        return frameScore + pipeScore + centeringBonus;
    }

    @Override
    public boolean isDone(Agent agent) {
        Bird bird = (Bird) agent;
        if (!bird.isAlive()) return true;

        bird.incrementFramesSurvived();

        for (Pipe p : pipes) {
            if (p.collides(bird)) {
                bird.setAlive(false);
                return true;
            }
            if (!p.isPassedBy(bird) && p.getX() + FlappyConfig.PIPE_WIDTH < bird.getX()) {
                p.setPassedBy(bird);
                bird.incrementPipesPassed();
            }
        }
        return false;
    }

    @Override
    public int getInputSize() {
        return 4;
    }

    @Override
    public int getOutputSize() {
        return 1;
    }
}
