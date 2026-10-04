package com.jane.gui;

import com.jane.gui.listeners.ControlActionListener;
import com.jane.gui.listeners.SimulationTickListener;
import com.jane.gui.panels.*;
import com.jane.util.AppConfig;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        super(AppConfig.APP_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // --- Evolve Mode Tab ---
        JPanel evolveTab = new JPanel(new BorderLayout());
        SimulationPanel simulationPanel = new SimulationPanel();
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        NetworkVisualizerPanel visualizerPanel = new NetworkVisualizerPanel();
        EvolutionGraphPanel graphPanel = new EvolutionGraphPanel();
        
        simulationPanel.addTickListener(new SimulationTickListener() {
            @Override
            public void onTick() {
                com.jane.flappy.Bird best = simulationPanel.getBestAliveBird();
                visualizerPanel.updateBestBird(best);
            }
        });
        
        simulationPanel.addGenerationListener(new com.jane.gui.listeners.SimulationGenerationListener() {
            @Override
            public void onGenerationComplete(int generation, double bestFitness) {
                graphPanel.addDataPoint(bestFitness);
            }
        });
        
        EvolveControlPanel controlPanel = new EvolveControlPanel(simulationPanel, new ControlActionListener() {
            @Override
            public void onStart() {
                simulationPanel.startSimulation();
            }

            @Override
            public void onPause() {
                simulationPanel.pauseSimulation();
            }

            @Override
            public void onReset() {
                simulationPanel.resetSimulation();
                graphPanel.reset();
            }

            @Override
            public void onLoadBestBrain() {
                simulationPanel.loadBestBrain();
            }
        });
        
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1));
        chartsPanel.add(visualizerPanel);
        chartsPanel.add(graphPanel);
        
        rightPanel.add(chartsPanel, BorderLayout.CENTER);
        rightPanel.add(controlPanel, BorderLayout.SOUTH);
        
        evolveTab.add(simulationPanel, BorderLayout.CENTER);
        evolveTab.add(rightPanel, BorderLayout.EAST);
        
        // --- Predict Mode Tab ---
        JPanel predictTab = new JPanel(new BorderLayout());
        WeatherPanel weatherPanel = new WeatherPanel();
        WeatherControlPanel weatherControlPanel = new WeatherControlPanel(weatherPanel);
        
        predictTab.add(weatherPanel, BorderLayout.CENTER);
        predictTab.add(weatherControlPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Evolve Mode (Flappy Bird)", evolveTab);
        tabbedPane.addTab("Predict Mode (Weather)", predictTab);
        
        add(tabbedPane);
    }
}
