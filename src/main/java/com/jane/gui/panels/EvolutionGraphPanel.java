package com.jane.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EvolutionGraphPanel extends JPanel {
    private List<Double> history = new ArrayList<>();
    
    public EvolutionGraphPanel() {
        setPreferredSize(new Dimension(300, 200));
        setBackground(new Color(240, 240, 240));
    }
    
    public void addDataPoint(double bestFitness) {
        history.add(bestFitness);
        repaint();
    }
    
    public void reset() {
        history.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (history.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 20;

        double maxFitness = history.stream().max(Double::compareTo).orElse(1.0);
        if (maxFitness == 0) maxFitness = 1.0;

        double xScale = (double) (w - 2 * padding) / Math.max(1, history.size() - 1);
        double yScale = (double) (h - 2 * padding) / maxFitness;

        g2d.setColor(Color.BLUE);
        for (int i = 0; i < history.size() - 1; i++) {
            int x1 = padding + (int) (i * xScale);
            int y1 = h - padding - (int) (history.get(i) * yScale);
            int x2 = padding + (int) ((i + 1) * xScale);
            int y2 = h - padding - (int) (history.get(i + 1) * yScale);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
