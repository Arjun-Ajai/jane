package com.jane.predict.weather;

import com.jane.predict.DataNormalizer;
import com.jane.predict.Dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

public class WeatherDataset implements Dataset {
    private List<double[]> inputs = new ArrayList<>();
    private List<double[]> targets = new ArrayList<>();
    private List<Integer> shuffledIndices = new ArrayList<>();
    private List<LocalDate> targetDates = new ArrayList<>();
    
    private DataNormalizer normalizer;
    private int windowSize = 7; // past 7 days
    
    public WeatherDataset(List<WeatherRecord> records, DataNormalizer normalizer, boolean isTraining) {
        this.normalizer = normalizer;
        
        // Ensure records are sorted by date
        records.sort((a, b) -> a.date().compareTo(b.date()));
        
        // Build sliding window
        // For each day i from windowSize to records.size() - 1
        // Input: records [i-windowSize to i-1]
        // Target: record [i].tempMax
        
        double[][] rawInputs = new double[Math.max(0, records.size() - windowSize)][];
        double[] rawTargets = new double[Math.max(0, records.size() - windowSize)];
        
        for (int i = windowSize; i < records.size(); i++) {
            double[] windowFeatures = new double[windowSize * 5]; // 5 features: max, min, hum, press, wind
            int idx = 0;
            for (int j = i - windowSize; j < i; j++) {
                WeatherRecord r = records.get(j);
                windowFeatures[idx++] = r.tempMax();
                windowFeatures[idx++] = r.tempMin();
                windowFeatures[idx++] = r.humidity();
                windowFeatures[idx++] = r.pressure();
                windowFeatures[idx++] = r.windSpeed();
            }
            rawInputs[i - windowSize] = windowFeatures;
            rawTargets[i - windowSize] = records.get(i).tempMax();
            targetDates.add(records.get(i).date());
        }
        
        if (isTraining) {
            normalizer.fit(rawInputs, rawTargets);
        }
        
        for (int i = 0; i < rawInputs.length; i++) {
            inputs.add(normalizer.normalizeFeatures(rawInputs[i]));
            targets.add(normalizer.normalizeTargets(new double[]{rawTargets[i]}));
            shuffledIndices.add(i);
        }
    }

    @Override
    public int size() {
        return inputs.size();
    }

    @Override
    public double[] inputsFor(int index) {
        return inputs.get(index);
    }

    @Override
    public double[] targetsFor(int index) {
        return targets.get(index);
    }

    @Override
    public List<Integer> shuffledIndices() {
        List<Integer> list = new ArrayList<>(shuffledIndices);
        Collections.shuffle(list);
        return list;
    }
    
    public DataNormalizer getNormalizer() {
        return normalizer;
    }

    public LocalDate dateFor(int index) {
        return targetDates.get(index);
    }
}
