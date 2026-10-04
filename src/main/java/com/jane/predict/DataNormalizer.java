package com.jane.predict;

public class DataNormalizer {
    private double minTarget;
    private double maxTarget;
    private double[] minFeatures;
    private double[] maxFeatures;

    public void fit(double[][] allFeatures, double[] allTargets) {
        if (allFeatures.length == 0) return;
        int featureCount = allFeatures[0].length;
        
        minFeatures = new double[featureCount];
        maxFeatures = new double[featureCount];
        
        for (int j = 0; j < featureCount; j++) {
            minFeatures[j] = Double.MAX_VALUE;
            maxFeatures[j] = -Double.MAX_VALUE;
        }
        
        minTarget = Double.MAX_VALUE;
        maxTarget = -Double.MAX_VALUE;

        for (int i = 0; i < allFeatures.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                if (allFeatures[i][j] < minFeatures[j]) minFeatures[j] = allFeatures[i][j];
                if (allFeatures[i][j] > maxFeatures[j]) maxFeatures[j] = allFeatures[i][j];
            }
            if (allTargets[i] < minTarget) minTarget = allTargets[i];
            if (allTargets[i] > maxTarget) maxTarget = allTargets[i];
        }
    }

    public double[] normalizeFeatures(double[] features) {
        double[] normalized = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            double range = maxFeatures[i] - minFeatures[i];
            if (range == 0) normalized[i] = 0;
            else normalized[i] = (features[i] - minFeatures[i]) / range;
        }
        return normalized;
    }

    public double[] normalizeTargets(double[] targets) {
        double[] normalized = new double[targets.length];
        for (int i = 0; i < targets.length; i++) {
            double range = maxTarget - minTarget;
            if (range == 0) normalized[i] = 0;
            else normalized[i] = (targets[i] - minTarget) / range;
        }
        return normalized;
    }
    
    public double denormalizeTarget(double normalizedTarget) {
        double range = maxTarget - minTarget;
        return normalizedTarget * range + minTarget;
    }
}
