package com.jane.core;

public interface Trainable {
    void mutate(double rate);
    // crossover is usually static, but we can't easily enforce a static method in an interface that returns itself.
    // The requirement says:
    // `NeuralNetwork implements Trainable (interface with mutate(double) and crossover(...) contractually declared) so the genetic algorithm code depends only on the interface`
    // So we can make crossover an instance method that takes another Trainable and returns a new Trainable.
    Trainable crossover(Trainable other);
}
