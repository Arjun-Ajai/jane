package com.jane.gui.panels;

import com.jane.core.Matrix;
import com.jane.core.NeuralNetwork;
import com.jane.flappy.Bird;

import javax.swing.*;
import java.awt.*;

public class NetworkVisualizerPanel extends JPanel {
    private Bird bestBird;

    public NetworkVisualizerPanel() {
        setPreferredSize(new Dimension(300, 300));
        setBackground(new Color(240, 240, 240));
    }

    public void updateBestBird(Bird bird) {
        this.bestBird = bird;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (bestBird == null || bestBird.getBrain() == null) {
            g.drawString("No brain to visualize", 10, 20);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        NeuralNetwork brain = bestBird.getBrain();
        int[] layerSizes = {brain.getInputNodes(), brain.getHiddenNodes(), brain.getOutputNodes()};
        
        int w = getWidth();
        int h = getHeight();
        int padding = 40;
        
        int numLayers = layerSizes.length;
        double layerSpacing = (double) (w - 2 * padding) / (numLayers - 1);

        // Calculate positions
        Point[][] nodePositions = new Point[numLayers][];
        for (int l = 0; l < numLayers; l++) {
            int nodesInLayer = layerSizes[l];
            nodePositions[l] = new Point[nodesInLayer];
            double ySpacing = (double) (h - 2 * padding) / (nodesInLayer > 1 ? nodesInLayer - 1 : 1);
            
            for (int n = 0; n < nodesInLayer; n++) {
                int x = padding + (int) (l * layerSpacing);
                int y = padding + (nodesInLayer == 1 ? (h - 2*padding)/2 : (int) (n * ySpacing));
                nodePositions[l][n] = new Point(x, y);
            }
        }

        // Draw connections
        drawConnections(g2d, brain.getWeightsIH(), nodePositions[0], nodePositions[1]);
        drawConnections(g2d, brain.getWeightsHO(), nodePositions[1], nodePositions[2]);

        // Draw nodes
        int radius = 10;
        for (int l = 0; l < numLayers; l++) {
            for (int n = 0; n < layerSizes[l]; n++) {
                Point p = nodePositions[l][n];
                g2d.setColor(Color.WHITE);
                g2d.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                g2d.setColor(Color.BLACK);
                g2d.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
            }
        }
    }

    private void drawConnections(Graphics2D g2d, Matrix weights, Point[] fromNodes, Point[] toNodes) {
        for (int i = 0; i < weights.rows; i++) { // to
            for (int j = 0; j < weights.cols; j++) { // from
                double weight = weights.data[i][j];
                Point from = fromNodes[j];
                Point to = toNodes[i];
                
                int alpha = Math.min(255, Math.max(0, (int) (Math.abs(weight) * 255)));
                if (weight > 0) {
                    g2d.setColor(new Color(0, 0, 255, alpha));
                } else {
                    g2d.setColor(new Color(255, 0, 0, alpha));
                }
                
                float thickness = Math.min(3.0f, (float) Math.abs(weight));
                g2d.setStroke(new BasicStroke(thickness));
                g2d.drawLine(from.x, from.y, to.x, to.y);
            }
        }
    }
}
