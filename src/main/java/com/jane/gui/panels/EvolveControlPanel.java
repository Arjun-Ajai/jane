package com.jane.gui.panels;

import com.jane.gui.listeners.ControlActionListener;
import com.jane.util.AppConfig;

import javax.swing.*;
import java.awt.*;

public class EvolveControlPanel extends JPanel {
    
    private ControlActionListener actionListener;
    private SimulationPanel simulationPanel;
    
    public EvolveControlPanel(SimulationPanel simPanel, ControlActionListener listener) {
        this.simulationPanel = simPanel;
        this.actionListener = listener;
        
        setLayout(new GridLayout(7, 1, 5, 5));
        setPreferredSize(new Dimension(300, 250));
        
        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(e -> actionListener.onStart());
        
        JButton btnPause = new JButton("Pause");
        btnPause.addActionListener(e -> actionListener.onPause());
        
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> actionListener.onReset());
        
        JButton btnLoadBest = new JButton("Load Best Brain");
        btnLoadBest.addActionListener(e -> actionListener.onLoadBestBrain());
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.add(btnStart);
        buttonPanel.add(btnPause);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnLoadBest);
        
        add(buttonPanel);
        
        // Speed slider
        add(new JLabel("Simulation Speed"));
        JSlider speedSlider = new JSlider(1, 10, 1);
        speedSlider.addChangeListener(e -> simulationPanel.setSimulationSpeed(speedSlider.getValue()));
        add(speedSlider);
        
        // Mutation rate slider
        add(new JLabel("Mutation Rate (0-50%)"));
        JSlider mutationSlider = new JSlider(0, 50, (int)(AppConfig.MUTATION_RATE * 100));
        mutationSlider.addChangeListener(e -> simulationPanel.setMutationRate(mutationSlider.getValue() / 100.0));
        add(mutationSlider);
    }
}
