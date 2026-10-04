package com.jane.predict;

import com.jane.core.BackpropTrainer;
import com.jane.core.NeuralNetwork;
import com.jane.core.activations.Sigmoid;
import com.jane.db.SessionDAO;
import com.jane.db.WeatherRunDAO;

public class Predictor {
    private NeuralNetwork net;
    private DataNormalizer normalizer;
    private WeatherRunDAO runDAO = new WeatherRunDAO();
    private SessionDAO sessionDAO = new SessionDAO();
    
    private int sessionId = -1;

    public Predictor(int inputSize) {
        // e.g. 7 days * 5 features = 35 inputs. Output is 1 (next day max temp).
        net = new NeuralNetwork(inputSize, 16, 1, new Sigmoid());
        
        try {
            sessionId = sessionDAO.createSession("PREDICT", "Weather Forecaster");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void train(Dataset trainSet, int epochs, double learningRate, java.util.function.Consumer<Double> progressCallback) {
        if (trainSet instanceof com.jane.predict.weather.WeatherDataset) {
            this.normalizer = ((com.jane.predict.weather.WeatherDataset) trainSet).getNormalizer();
        }
        
        BackpropTrainer trainer = new BackpropTrainer();
        for (int e = 1; e <= epochs; e++) {
            trainer.trainEpoch(net, trainSet, learningRate);
            double loss = trainer.evaluateLoss(net, trainSet);
            
            if (sessionId != -1 && e % 10 == 0) { // save every 10 epochs
                try {
                    runDAO.insertRun(sessionId, e, loss);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            if (progressCallback != null) {
                progressCallback.accept(loss);
            }
        }
    }

    public double predictNextTempMax(double[] recentDaysFeaturesArray) {
        if (normalizer == null) {
            throw new IllegalStateException("Normalizer not initialized. Train first.");
        }
        double[] normalizedInputs = normalizer.normalizeFeatures(recentDaysFeaturesArray);
        return predictNormalized(normalizedInputs);
    }
    
    public double predictNormalized(double[] normalizedInputs) {
        if (normalizer == null) {
            throw new IllegalStateException("Normalizer not initialized. Train first.");
        }
        double[] normalizedOutput = net.predict(normalizedInputs);
        return normalizer.denormalizeTarget(normalizedOutput[0]);
    }
    
    public int getSessionId() {
        return sessionId;
    }
}
