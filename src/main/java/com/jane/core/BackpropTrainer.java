package com.jane.core;

import com.jane.predict.Dataset;
import java.util.List;

public class BackpropTrainer {
    public void trainEpoch(NeuralNetwork net, Dataset dataset, double learningRate) {
        List<Integer> shuffledIndices = dataset.shuffledIndices();
        for (int i : shuffledIndices) {
            double[] inputs = dataset.inputsFor(i);
            double[] targets = dataset.targetsFor(i);
            net.train(inputs, targets, learningRate);
        }
    }

    public double evaluateLoss(NeuralNetwork net, Dataset dataset) {
        double totalLoss = 0;
        int n = dataset.size();
        for (int i = 0; i < n; i++) {
            double[] inputs = dataset.inputsFor(i);
            double[] targets = dataset.targetsFor(i);
            double[] outputs = net.predict(inputs);
            
            double sampleLoss = 0;
            for (int j = 0; j < targets.length; j++) {
                double error = targets[j] - outputs[j];
                sampleLoss += error * error;
            }
            totalLoss += sampleLoss / targets.length;
        }
        return totalLoss / n; // Mean Squared Error
    }
}
