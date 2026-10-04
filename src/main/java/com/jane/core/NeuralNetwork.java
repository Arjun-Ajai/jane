package com.jane.core;

public class NeuralNetwork implements Trainable {
    private final int inputNodes;
    private final int hiddenNodes;
    private final int outputNodes;
    
    private Matrix weightsIH;
    private Matrix weightsHO;
    private Matrix biasH;
    private Matrix biasO;
    
    private final ActivationFunction activation;

    public NeuralNetwork(int inputNodes, int hiddenNodes, int outputNodes, ActivationFunction activation) {
        this.inputNodes = inputNodes;
        this.hiddenNodes = hiddenNodes;
        this.outputNodes = outputNodes;
        this.activation = activation;
        
        this.weightsIH = new Matrix(hiddenNodes, inputNodes).randomize(-1, 1);
        this.weightsHO = new Matrix(outputNodes, hiddenNodes).randomize(-1, 1);
        this.biasH = new Matrix(hiddenNodes, 1).randomize(-1, 1);
        this.biasO = new Matrix(outputNodes, 1).randomize(-1, 1);
    }
    
    private NeuralNetwork(NeuralNetwork other) {
        this.inputNodes = other.inputNodes;
        this.hiddenNodes = other.hiddenNodes;
        this.outputNodes = other.outputNodes;
        this.activation = other.activation;
        
        this.weightsIH = other.weightsIH.copy();
        this.weightsHO = other.weightsHO.copy();
        this.biasH = other.biasH.copy();
        this.biasO = other.biasO.copy();
    }

    public double[] predict(double[] inputsArray) {
        Matrix inputs = Matrix.fromArray(inputsArray);
        
        // hidden = activation(weightsIH * inputs + biasH)
        Matrix hidden = this.weightsIH.multiply(inputs);
        hidden = hidden.add(this.biasH);
        hidden = hidden.applyFunction(activation::apply);
        
        // output = activation(weightsHO * hidden + biasO)
        Matrix output = this.weightsHO.multiply(hidden);
        output = output.add(this.biasO);
        output = output.applyFunction(activation::apply);
        
        return output.toArray();
    }

    public NeuralNetwork copy() {
        return new NeuralNetwork(this);
    }

    @Override
    public void mutate(double rate) {
        java.util.Random random = new java.util.Random();
        java.util.function.DoubleUnaryOperator mutator = val -> {
            if (Math.random() < rate) {
                return val + random.nextGaussian() * 0.3;
            }
            return val;
        };
        
        this.weightsIH = this.weightsIH.applyFunction(mutator);
        this.weightsHO = this.weightsHO.applyFunction(mutator);
        this.biasH = this.biasH.applyFunction(mutator);
        this.biasO = this.biasO.applyFunction(mutator);
    }
    
    public static NeuralNetwork crossover(NeuralNetwork a, NeuralNetwork b) {
        NeuralNetwork child = new NeuralNetwork(a.inputNodes, a.hiddenNodes, a.outputNodes, a.activation);
        crossoverMatrix(a.weightsIH, b.weightsIH, child.weightsIH);
        crossoverMatrix(a.weightsHO, b.weightsHO, child.weightsHO);
        crossoverMatrix(a.biasH, b.biasH, child.biasH);
        crossoverMatrix(a.biasO, b.biasO, child.biasO);
        return child;
    }
    
    private static void crossoverMatrix(Matrix a, Matrix b, Matrix child) {
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                child.data[i][j] = Math.random() < 0.5 ? a.data[i][j] : b.data[i][j];
            }
        }
    }

    @Override
    public Trainable crossover(Trainable other) {
        if (!(other instanceof NeuralNetwork)) {
            throw new IllegalArgumentException("Cannot crossover with a different type of Trainable");
        }
        return crossover(this, (NeuralNetwork) other);
    }

    public void train(double[] inputsArray, double[] targetsArray, double learningRate) {
        Matrix inputs = Matrix.fromArray(inputsArray);
        Matrix targets = Matrix.fromArray(targetsArray);
        
        // 1. Forward pass
        Matrix hidden = this.weightsIH.multiply(inputs).add(this.biasH).applyFunction(activation::apply);
        Matrix output = this.weightsHO.multiply(hidden).add(this.biasO).applyFunction(activation::apply);
        
        // 2. Output layer error and gradient
        Matrix outputErrors = targets.subtract(output);
        Matrix outputGradient = output.applyFunction(activation::derivative)
                .multiplyElementwise(outputErrors)
                .multiply(learningRate);
        
        // 3. Update hidden->output weights
        Matrix hiddenT = hidden.transpose();
        Matrix weightsHODeltas = outputGradient.multiply(hiddenT);
        this.weightsHO = this.weightsHO.add(weightsHODeltas);
        this.biasO = this.biasO.add(outputGradient);
        
        // 4. Backpropagate error to hidden layer
        // Using the OLD weightsHO for error backpropagation is standard in some implementations, 
        // but here we already updated weightsHO. To be precise with textbook backprop we should use 
        // the original weightsHO. Let's use the transpose of the updated one, it usually converges similarly.
        // Wait, standard backprop usually calculates all deltas before applying them.
        // Let's adjust to calculate all deltas first.
        
        Matrix weightsHOT = this.weightsHO.subtract(weightsHODeltas).transpose(); // use pre-update weights
        Matrix hiddenErrors = weightsHOT.multiply(outputErrors);
        Matrix hiddenGradient = hidden.applyFunction(activation::derivative)
                .multiplyElementwise(hiddenErrors)
                .multiply(learningRate);
                
        // 5. Update input->hidden weights
        Matrix inputsT = inputs.transpose();
        Matrix weightsIHDeltas = hiddenGradient.multiply(inputsT);
        this.weightsIH = this.weightsIH.add(weightsIHDeltas);
        this.biasH = this.biasH.add(hiddenGradient);
    }

    public void save(String filepath) {
        // Will delegate to DB in GenomeDAO, or implement simple serialization if requested
        throw new UnsupportedOperationException("Persistence is handled via GenomeDAO");
    }
    
    public static NeuralNetwork load(String filepath) {
        throw new UnsupportedOperationException("Persistence is handled via GenomeDAO");
    }
    
    public String serializeWeights() {
        // Simple serialization logic to store in JSON/String format for SQLite
        StringBuilder sb = new StringBuilder();
        serializeMatrix(sb, weightsIH);
        sb.append(";");
        serializeMatrix(sb, weightsHO);
        sb.append(";");
        serializeMatrix(sb, biasH);
        sb.append(";");
        serializeMatrix(sb, biasO);
        return sb.toString();
    }
    
    public void deserializeWeights(String data) {
        String[] parts = data.split(";");
        if (parts.length != 4) {
            throw new com.jane.core.exceptions.CorruptSaveFileException("Invalid weights data format");
        }
        deserializeMatrix(parts[0], weightsIH);
        deserializeMatrix(parts[1], weightsHO);
        deserializeMatrix(parts[2], biasH);
        deserializeMatrix(parts[3], biasO);
    }
    
    private void serializeMatrix(StringBuilder sb, Matrix m) {
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                sb.append(m.data[i][j]);
                if (j < m.cols - 1) sb.append(",");
            }
            if (i < m.rows - 1) sb.append("|");
        }
    }
    
    private void deserializeMatrix(String data, Matrix m) {
        String[] rows = data.split("\\|");
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            for (int j = 0; j < cols.length; j++) {
                m.data[i][j] = Double.parseDouble(cols[j]);
            }
        }
    }
    
    public Matrix getWeightsIH() { return weightsIH; }
    public Matrix getWeightsHO() { return weightsHO; }
    public Matrix getBiasH() { return biasH; }
    public Matrix getBiasO() { return biasO; }
    public int getInputNodes() { return inputNodes; }
    public int getHiddenNodes() { return hiddenNodes; }
    public int getOutputNodes() { return outputNodes; }
    public ActivationFunction getActivation() { return activation; }
}
