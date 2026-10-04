package com.jane.gui.panels;

import com.jane.core.GeneticAlgorithm;
import com.jane.core.Genome;
import com.jane.core.NeuralNetwork;
import com.jane.core.activations.Sigmoid;
import com.jane.db.GenerationDAO;
import com.jane.db.GenomeDAO;
import com.jane.db.SessionDAO;
import com.jane.env.Agent;
import com.jane.flappy.Bird;
import com.jane.flappy.FlappyBirdEnvironment;
import com.jane.flappy.FlappyConfig;
import com.jane.gui.listeners.SimulationTickListener;
import com.jane.util.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SimulationPanel extends JPanel implements ActionListener {
    private Timer timer;
    private FlappyBirdEnvironment environment;
    private List<Bird> population;
    private GeneticAlgorithm geneticAlgorithm;
    
    private int currentGeneration = 1;
    private int sessionId = -1;
    private boolean isBestBrainMode = false;
    
    private SessionDAO sessionDAO = new SessionDAO();
    private GenerationDAO generationDAO = new GenerationDAO();
    private GenomeDAO genomeDAO = new GenomeDAO();
    
    private java.util.LinkedList<Boolean> winHistory = new java.util.LinkedList<>();
    
    private List<SimulationTickListener> tickListeners = new ArrayList<>();
    private List<com.jane.gui.listeners.SimulationGenerationListener> genListeners = new ArrayList<>();
    
    public SimulationPanel() {
        setPreferredSize(new Dimension(FlappyConfig.WINDOW_WIDTH, FlappyConfig.WINDOW_HEIGHT));
        setBackground(new Color(135, 206, 235)); // Sky blue
        
        environment = new FlappyBirdEnvironment();
        geneticAlgorithm = new GeneticAlgorithm(AppConfig.POPULATION_SIZE, AppConfig.MUTATION_RATE, AppConfig.ELITE_PERCENT);
        
        timer = new Timer(16, this); // ~60fps
        initPopulation();
    }
    
    public void addTickListener(SimulationTickListener listener) {
        tickListeners.add(listener);
    }
    
    public void addGenerationListener(com.jane.gui.listeners.SimulationGenerationListener listener) {
        genListeners.add(listener);
    }
    
    private void initPopulation() {
        population = new ArrayList<>();
        for (int i = 0; i < geneticAlgorithm.getPopulationSize(); i++) {
            NeuralNetwork brain = new NeuralNetwork(4, AppConfig.HIDDEN_NODES, 1, new Sigmoid());
            population.add(new Bird(brain));
        }
        currentGeneration = 1;
        
        // create DB session
        try {
            sessionId = sessionDAO.createSession("EVOLVE", "Flappy Bird");
        } catch (Exception e) {
            e.printStackTrace(); // ignore if DB not ready
        }
    }
    
    public void startSimulation() {
        timer.start();
    }
    
    public void pauseSimulation() {
        timer.stop();
    }
    
    public void resetSimulation() {
        isBestBrainMode = false;
        environment.reset();
        initPopulation();
        repaint();
    }
    
    public void loadBestBrain() {
        if (sessionId == -1) return;
        NeuralNetwork best = null;
        try {
            best = genomeDAO.findBest(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (best != null) {
            isBestBrainMode = true;
            timer.stop();
            environment.reset();
            population.clear();
            population.add(new Bird(best)); // Only 1 bird
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "No best brain found for this session!");
        }
    }

    public void setSimulationSpeed(int speedMultiplier) {
        timer.setDelay(Math.max(1, 16 / speedMultiplier));
    }
    
    public void setMutationRate(double rate) {
        geneticAlgorithm.setMutationRate(rate);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean allDead = true;
        
        // Single update step
        environment.update();
        
        for (Bird bird : population) {
            if (bird.isAlive()) {
                allDead = false;
                double[] inputs = environment.getState(bird);
                double[] output = bird.decide(inputs);
                environment.applyAction(bird, output);
                bird.update();
                environment.isDone(bird); // check collision and update fitness
            }
        }
        
        if (allDead) {
            if (isBestBrainMode) {
                // If it was just one best brain, reset environment for it again
                environment.reset();
                for (Bird b : population) {
                    b.setAlive(true);
                }
            } else {
                nextGeneration();
            }
        }
        
        repaint();
        
        for (SimulationTickListener l : tickListeners) {
            l.onTick();
        }
    }
    
    private void nextGeneration() {
        List<Genome> currentGenomes = new ArrayList<>();
        double totalFitness = 0;
        double bestFitness = -1;
        Genome bestGenome = null;
        int bestPipes = 0;
        
        for (Bird bird : population) {
            double fitness = environment.getFitness(bird);
            Genome g = new Genome(bird.getBrain(), currentGeneration);
            g.setFitness(fitness);
            currentGenomes.add(g);
            
            totalFitness += fitness;
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestGenome = g;
            }
            if (bird.getPipesPassed() > bestPipes) {
                bestPipes = bird.getPipesPassed();
            }
        }
        
        boolean win = bestPipes >= 20;
        winHistory.addLast(win);
        if (winHistory.size() > 50) {
            winHistory.removeFirst();
        }
        
        double winRate = 0;
        for (Boolean w : winHistory) {
            if (w) winRate += 1.0;
        }
        winRate /= winHistory.size();

        double avgFitness = totalFitness / population.size();
        
        System.out.printf(
            "gen=%d bestFitness=%.1f avgFitness=%.1f bestPipes=%d winRate(last50gens)=%.2f%n",
            currentGeneration, bestFitness, avgFitness, bestPipes, winRate
        );
        
        if (sessionId != -1) {
            try {
                int genId = generationDAO.insertGeneration(sessionId, currentGeneration, bestFitness, avgFitness);
                // only save best to avoid bloat
                if (bestGenome != null) {
                    genomeDAO.insertGenome(genId, bestGenome.getBrain(), bestFitness, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        List<Genome> nextGen = geneticAlgorithm.nextGeneration(currentGenomes);
        
        population.clear();
        for (Genome g : nextGen) {
            population.add(new Bird(g.getBrain()));
        }
        
        long nextSeed = System.nanoTime();
        environment.reset(nextSeed);
        
        for (com.jane.gui.listeners.SimulationGenerationListener l : genListeners) {
            l.onGenerationComplete(currentGeneration, bestFitness);
        }
        
        currentGeneration++;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        environment.render(g2d);
        
        for (Bird bird : population) {
            bird.render(g2d);
        }
        
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Generation: " + currentGeneration, 10, 30);
        g2d.drawString("Alive: " + population.stream().filter(Agent::isAlive).count(), 10, 55);
    }
    
    public Bird getBestAliveBird() {
        Bird best = null;
        double maxF = -1;
        for (Bird b : population) {
            if (b.isAlive()) {
                double f = environment.getFitness(b);
                if (f > maxF) {
                    maxF = f;
                    best = b;
                }
            }
        }
        return best;
    }
}
