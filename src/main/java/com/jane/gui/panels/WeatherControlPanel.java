package com.jane.gui.panels;

import com.jane.db.WeatherRunDAO;
import com.jane.predict.DataNormalizer;
import com.jane.predict.Predictor;
import com.jane.predict.weather.WeatherCsvLoader;
import com.jane.predict.weather.WeatherDataset;
import com.jane.predict.weather.WeatherRecord;
import com.jane.util.CsvUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WeatherControlPanel extends JPanel {
    private WeatherPanel weatherPanel;
    private Predictor predictor;
    private WeatherDataset trainSet;
    private WeatherDataset testSet;
    
    private JButton btnLoad;
    private JButton btnTrain;
    private JButton btnReveal;
    private JButton btnPause;
    private JButton btnReplay;
    private JSlider revealSpeedSlider;
    
    private WeatherRunDAO weatherRunDAO = new WeatherRunDAO();

    public WeatherControlPanel(WeatherPanel weatherPanel) {
        this.weatherPanel = weatherPanel;
        
        setLayout(new FlowLayout(FlowLayout.LEFT));
        
        btnLoad = new JButton("Load Dataset");
        btnLoad.addActionListener(e -> loadDataset());
        
        btnTrain = new JButton("Train");
        btnTrain.setEnabled(false);
        btnTrain.addActionListener(e -> trainModel());

        btnReveal = new JButton("Reveal");
        btnReveal.setEnabled(false);
        btnReveal.addActionListener(e -> weatherPanel.startReveal(revealSpeedSlider.getValue()));

        btnPause = new JButton("Pause");
        btnPause.setEnabled(false);
        btnPause.addActionListener(e -> weatherPanel.pauseReveal());

        btnReplay = new JButton("Replay");
        btnReplay.setEnabled(false);
        btnReplay.addActionListener(e -> weatherPanel.replayReveal(revealSpeedSlider.getValue()));

        revealSpeedSlider = new JSlider(50, 500, 200);
        revealSpeedSlider.setToolTipText("Milliseconds between revealed points");
        
        add(btnLoad);
        add(btnTrain);
        add(btnReveal);
        add(btnPause);
        add(btnReplay);
        add(new JLabel("Reveal delay"));
        add(revealSpeedSlider);
    }

    private void loadDataset() {
        String filepath = "src/main/resources/data/weather_sample.csv";
        File file = new File(filepath);
        if (!file.exists() || needsPresentationDataset(file)) {
            CsvUtils.generateSyntheticWeatherCsv(filepath, 90);
            JOptionPane.showMessageDialog(this, "Generated September weather dataset at " + filepath);
        }
        
        List<WeatherRecord> records = WeatherCsvLoader.load(filepath);
        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found in dataset");
            return;
        }
        
        // Split 80/20
        int splitIdx = (int) (records.size() * 0.8);
        List<WeatherRecord> trainRecords = records.subList(0, splitIdx);
        List<WeatherRecord> testRecords = records.subList(splitIdx, records.size());
        
        DataNormalizer normalizer = new DataNormalizer();
        trainSet = new WeatherDataset(trainRecords, normalizer, true);
        testSet = new WeatherDataset(testRecords, normalizer, false); // use same normalizer
        
        btnTrain.setEnabled(true);
        weatherPanel.reset();
        JOptionPane.showMessageDialog(this, "Dataset loaded. Train set size: " + trainSet.size() + ", Test set size: " + testSet.size());
    }

    private boolean needsPresentationDataset(File file) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            reader.readLine();
            String firstRecord = reader.readLine();
            return firstRecord == null || !firstRecord.startsWith("2020-09-");
        } catch (java.io.IOException e) {
            return true;
        }
    }

    private void trainModel() {
        btnLoad.setEnabled(false);
        btnTrain.setEnabled(false);
        weatherPanel.reset();
        
        predictor = new Predictor(7 * 5); // 7 days * 5 features
        
        SwingWorker<Void, Double> worker = new SwingWorker<Void, Double>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Train for 100 epochs
                predictor.train(trainSet, 100, 0.1, loss -> {
                    publish(loss);
                });
                return null;
            }

            @Override
            protected void process(List<Double> chunks) {
                for (Double loss : chunks) {
                    weatherPanel.addLossPoint(loss);
                }
            }

            @Override
            protected void done() {
                btnLoad.setEnabled(true);
                btnTrain.setEnabled(true);
                evaluateModel();
                btnReveal.setEnabled(true);
                btnPause.setEnabled(true);
                btnReplay.setEnabled(true);
            }
        };
        
        worker.execute();
    }
    
    private void evaluateModel() {
        List<WeatherPredictionPoint> points = new ArrayList<>();
        
        for (int i = 0; i < testSet.size(); i++) {
            double[] inputs = testSet.inputsFor(i);
            double[] targetNormalized = testSet.targetsFor(i);
            
            double actualTemp = trainSet.getNormalizer().denormalizeTarget(targetNormalized[0]);
            double predictedTemp = predictor.predictNormalized(inputs);
            
            points.add(new WeatherPredictionPoint(testSet.dateFor(i), actualTemp, predictedTemp));
        }
        
        weatherPanel.setTestResults(points);
    }
}
