package com.jane.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneticAlgorithm {
    private int populationSize;
    private double mutationRate;
    private double elitePercent;

    public GeneticAlgorithm(int populationSize, double mutationRate, double elitePercent) {
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.elitePercent = elitePercent;
    }

    public List<Genome> nextGeneration(List<Genome> current) {
        if (current.isEmpty()) {
            throw new IllegalArgumentException("Current generation cannot be empty.");
        }

        // 1. Sort by fitness descending (Genome implements Comparable)
        Collections.sort(current);

        List<Genome> nextGen = new ArrayList<>(populationSize);
        int generationNumber = current.get(0).getGeneration() + 1;

        // 2. Keep top N% as elites
        int eliteCount = Math.max(1, (int) (populationSize * elitePercent));
        for (int i = 0; i < eliteCount && i < current.size(); i++) {
            // Elites pass through unmutated, but we should copy their brains
            NeuralNetwork eliteBrain = current.get(i).getBrain().copy();
            nextGen.add(new Genome(eliteBrain, generationNumber));
        }
        
        // 3. Inject a small number of fully random genomes each generation (immigrants)
        int immigrantCount = Math.max(1, (int)(populationSize * 0.05));
        if (!current.isEmpty()) {
            NeuralNetwork template = current.get(0).getBrain();
            for (int i = 0; i < immigrantCount && nextGen.size() < populationSize; i++) {
                NeuralNetwork randomBrain = new NeuralNetwork(
                        template.getInputNodes(), 
                        template.getHiddenNodes(), 
                        template.getOutputNodes(), 
                        template.getActivation()
                );
                nextGen.add(new Genome(randomBrain, generationNumber));
            }
        }

        // 3. Fill the rest via weighted-random parent selection (roulette wheel)
        double totalFitness = 0;
        for (Genome g : current) {
            // Avoid negative fitness issues, though flappy bird fitness is positive
            totalFitness += Math.max(0, g.getFitness());
        }

        while (nextGen.size() < populationSize) {
            Genome parentA = selectParent(current, totalFitness);
            Genome parentB = selectParent(current, totalFitness);

            NeuralNetwork childBrain = NeuralNetwork.crossover(parentA.getBrain(), parentB.getBrain());
            childBrain.mutate(mutationRate);
            
            nextGen.add(new Genome(childBrain, generationNumber));
        }

        return nextGen;
    }

    private Genome selectParent(List<Genome> population, double totalFitness) {
        if (totalFitness == 0) {
            // If all fitness is zero, pick random
            return population.get((int) (Math.random() * population.size()));
        }

        double rand = Math.random() * totalFitness;
        double runningSum = 0;

        for (Genome g : population) {
            runningSum += Math.max(0, g.getFitness());
            if (runningSum >= rand) {
                return g;
            }
        }
        
        // Fallback
        return population.get(population.size() - 1);
    }

    public int getPopulationSize() { return populationSize; }
    public void setPopulationSize(int populationSize) { this.populationSize = populationSize; }
    public double getMutationRate() { return mutationRate; }
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }
    public double getElitePercent() { return elitePercent; }
    public void setElitePercent(double elitePercent) { this.elitePercent = elitePercent; }
}
