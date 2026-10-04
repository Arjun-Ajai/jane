package com.jane.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherPanel extends JPanel {
    private final TickingRevealChartPanel chartPanel = new TickingRevealChartPanel();
    private final JLabel trainingStatus = new JLabel("Load a dataset and click Train", SwingConstants.CENTER);
    private boolean isTraining = false;

    public WeatherPanel() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        add(trainingStatus, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    public void addLossPoint(double loss) {
        trainingStatus.setText("Training... current loss: " + String.format("%.4f", loss));
    }
    
    public void setTestResults(List<WeatherPredictionPoint> points) {
        chartPanel.setData(points);
        this.isTraining = false;
        trainingStatus.setText("Held-out test predictions - click Reveal to animate");
        repaint();
    }
    
    public void reset() {
        chartPanel.stop();
        chartPanel.setData(List.of());
        isTraining = true;
        trainingStatus.setText("Training in progress...");
        repaint();
    }

    public void startReveal(int delayMs) {
        chartPanel.startReveal(delayMs);
    }

    public void pauseReveal() {
        chartPanel.pause();
    }

    public void replayReveal(int delayMs) {
        chartPanel.replay(delayMs);
    }
}
