package com.jane.core;

public class Genome implements Comparable<Genome> {
    private NeuralNetwork brain;
    private double fitness;
    private int generation;

    public Genome(NeuralNetwork brain, int generation) {
        this.brain = brain;
        this.generation = generation;
        this.fitness = 0.0;
    }

    public NeuralNetwork getBrain() {
        return brain;
    }

    public void setBrain(NeuralNetwork brain) {
        this.brain = brain;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    @Override
    public int compareTo(Genome other) {
        // Sort descending by fitness
        return Double.compare(other.fitness, this.fitness);
    }
}
