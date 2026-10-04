package com.jane.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TickingRevealChartPanel extends JPanel {
    private static final Color COLOR_ACTUAL = new Color(0x1B, 0x1A, 0x17);
    private static final Color COLOR_PREDICTED = new Color(0xB8, 0x86, 0x2E);
    private static final Color COLOR_GRID = new Color(0xE4, 0xE2, 0xDC);
    private static final Color COLOR_AXIS = new Color(0x6B, 0x69, 0x63);
    private static final int MARGIN_LEFT = 60;
    private static final int MARGIN_RIGHT = 24;
    private static final int MARGIN_TOP = 36;
    private static final int MARGIN_BOTTOM = 48;
    private static final long POP_DURATION_MS = 300L;

    private List<WeatherPredictionPoint> data = List.of();
    private int revealedCount;
    private long lastRevealTimestamp;
    private Timer revealTimer;
    private Timer animationTimer;

    public TickingRevealChartPanel() {
        setBackground(Color.WHITE);
    }

    public void setData(List<WeatherPredictionPoint> newData) {
        stop();
        data = List.copyOf(newData);
        repaint();
    }

    public void startReveal(int msPerPoint) {
        if (data.isEmpty()) {
            return;
        }
        if (revealTimer != null) {
            revealTimer.stop();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
        revealedCount = 0;
        revealTimer = new Timer(Math.max(1, msPerPoint), e -> {
            if (revealedCount >= data.size()) {
                revealTimer.stop();
                return;
            }
            revealedCount++;
            lastRevealTimestamp = System.currentTimeMillis();
            repaint();
        });
        revealTimer.start();
        animationTimer = new Timer(16, e -> {
            repaint();
            if (revealedCount >= data.size()
                    && System.currentTimeMillis() - lastRevealTimestamp > POP_DURATION_MS) {
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }

    public void pause() {
        if (revealTimer != null) {
            revealTimer.stop();
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public void replay(int msPerPoint) {
        startReveal(msPerPoint);
    }

    public void stop() {
        pause();
        revealedCount = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (data.isEmpty()) {
            drawEmptyState(g2);
            g2.dispose();
            return;
        }

        double minTemp = data.stream()
                .mapToDouble(p -> Math.min(p.actualTemp(), p.predictedTemp())).min().orElse(0);
        double maxTemp = data.stream()
                .mapToDouble(p -> Math.max(p.actualTemp(), p.predictedTemp())).max().orElse(1);
        double range = Math.max(1, maxTemp - minTemp);
        double pad = range * 0.12;
        minTemp = Math.max(0, minTemp - pad);
        maxTemp += pad;

        drawYAxisAndGridlines(g2, minTemp, maxTemp);
        drawXAxis(g2);
        drawLegend(g2);
        drawDataLines(g2, minTemp, maxTemp);
        drawRevealedPoints(g2, minTemp, maxTemp);
        g2.dispose();
    }

    private int plotWidth() {
        return Math.max(1, getWidth() - MARGIN_LEFT - MARGIN_RIGHT);
    }

    private int plotHeight() {
        return Math.max(1, getHeight() - MARGIN_TOP - MARGIN_BOTTOM);
    }

    private int xToScreen(int index) {
        if (data.size() <= 1) {
            return MARGIN_LEFT;
        }
        return MARGIN_LEFT + (int) ((double) index / (data.size() - 1) * plotWidth());
    }

    private int yToScreen(double temp, double minTemp, double maxTemp) {
        double normalized = (temp - minTemp) / (maxTemp - minTemp);
        return MARGIN_TOP + (int) ((1 - normalized) * plotHeight());
    }

    private void drawYAxisAndGridlines(Graphics2D g2, double minTemp, double maxTemp) {
        int divisions = 5;
        g2.setFont(getFont().deriveFont(11f));
        FontMetrics metrics = g2.getFontMetrics();
        for (int i = 0; i <= divisions; i++) {
            double value = minTemp + (maxTemp - minTemp) * i / divisions;
            int y = yToScreen(value, minTemp, maxTemp);
            g2.setColor(COLOR_GRID);
            g2.drawLine(MARGIN_LEFT, y, getWidth() - MARGIN_RIGHT, y);
            g2.setColor(COLOR_AXIS);
            String label = String.format("%.0f°C", value);
            g2.drawString(label, MARGIN_LEFT - metrics.stringWidth(label) - 8,
                    y + metrics.getAscent() / 2 - 2);
        }
        g2.setColor(COLOR_AXIS);
        g2.drawLine(MARGIN_LEFT, MARGIN_TOP, MARGIN_LEFT, getHeight() - MARGIN_BOTTOM);
    }

    private void drawXAxis(Graphics2D g2) {
        int axisY = getHeight() - MARGIN_BOTTOM;
        g2.setColor(COLOR_AXIS);
        g2.drawLine(MARGIN_LEFT, axisY, getWidth() - MARGIN_RIGHT, axisY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
        int labelEvery = Math.max(1, data.size() / 8);
        g2.setFont(getFont().deriveFont(10.5f));
        FontMetrics metrics = g2.getFontMetrics();
        for (int i = 0; i < data.size(); i += labelEvery) {
            int x = xToScreen(i);
            g2.drawLine(x, axisY, x, axisY + 4);
            String label = data.get(i).date().format(formatter);
            g2.drawString(label, x - metrics.stringWidth(label) / 2, axisY + 18);
        }
    }

    private void drawLegend(Graphics2D g2) {
        int x = Math.max(MARGIN_LEFT, getWidth() - MARGIN_RIGHT - 150);
        g2.setFont(getFont().deriveFont(11f));
        g2.setColor(COLOR_ACTUAL);
        g2.fillRect(x, 14, 14, 3);
        g2.drawString("Actual", x + 20, 20);
        g2.setColor(COLOR_PREDICTED);
        g2.fillRect(x + 90, 14, 14, 3);
        g2.drawString("Predicted", x + 110, 20);
    }

    private void drawDataLines(Graphics2D g2, double minTemp, double maxTemp) {
        if (revealedCount < 2) {
            return;
        }
        Path2D actual = new Path2D.Double();
        Path2D predicted = new Path2D.Double();
        for (int i = 0; i < revealedCount; i++) {
            WeatherPredictionPoint point = data.get(i);
            int x = xToScreen(i);
            int actualY = yToScreen(point.actualTemp(), minTemp, maxTemp);
            int predictedY = yToScreen(point.predictedTemp(), minTemp, maxTemp);
            if (i == 0) {
                actual.moveTo(x, actualY);
                predicted.moveTo(x, predictedY);
            } else {
                actual.lineTo(x, actualY);
                predicted.lineTo(x, predictedY);
            }
        }
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(COLOR_ACTUAL);
        g2.draw(actual);
        g2.setColor(COLOR_PREDICTED);
        g2.draw(predicted);
    }

    private void drawRevealedPoints(Graphics2D g2, double minTemp, double maxTemp) {
        if (revealedCount == 0) {
            return;
        }
        for (int i = 0; i < revealedCount - 1; i++) {
            drawDot(g2, i, minTemp, maxTemp, 3f, 1f);
        }
        float progress = Math.min(1f,
                (System.currentTimeMillis() - lastRevealTimestamp) / (float) POP_DURATION_MS);
        drawDot(g2, revealedCount - 1, minTemp, maxTemp,
                6f * (1 - progress) + 3f * progress, progress);
    }

    private void drawDot(Graphics2D g2, int index, double minTemp, double maxTemp,
                         float radius, float alpha) {
        WeatherPredictionPoint point = data.get(index);
        int x = xToScreen(index);
        int actualY = yToScreen(point.actualTemp(), minTemp, maxTemp);
        int predictedY = yToScreen(point.predictedTemp(), minTemp, maxTemp);
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(COLOR_ACTUAL);
        g2.fill(new Ellipse2D.Double(x - radius, actualY - radius, radius * 2, radius * 2));
        g2.setColor(COLOR_PREDICTED);
        g2.fill(new Ellipse2D.Double(x - radius, predictedY - radius, radius * 2, radius * 2));
        g2.setComposite(old);
    }

    private void drawEmptyState(Graphics2D g2) {
        g2.setColor(COLOR_AXIS);
        String message = "Train the model to see predictions revealed here.";
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, getHeight() / 2);
    }
}
