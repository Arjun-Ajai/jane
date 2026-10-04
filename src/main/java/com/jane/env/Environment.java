package com.jane.env;

public interface Environment {
    double[] getState(Agent agent);          
    void applyAction(Agent agent, double[] action); 
    double getFitness(Agent agent);          
    boolean isDone(Agent agent);             
    void reset();                 
    int getInputSize();
    int getOutputSize();
}
