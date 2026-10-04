package com.jane.env;

import com.jane.core.NeuralNetwork;
import java.awt.Graphics2D;

public abstract class Agent {
    protected NeuralNetwork brain;
    protected boolean alive = true;

    public Agent(NeuralNetwork brain) {
        this.brain = brain;
    }

    public abstract void update();
    public abstract void render(Graphics2D g);

    public double[] decide(double[] inputs) {
        if (brain != null) {
            return brain.predict(inputs);
        }
        return new double[0];
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public NeuralNetwork getBrain() {
        return brain;
    }
}
