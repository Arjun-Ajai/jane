package com.jane.core.activations;

import com.jane.core.ActivationFunction;

public class Sigmoid implements ActivationFunction {
    @Override
    public double apply(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    @Override
    public double derivative(double x) {
        // x here is assumed to be the already-activated value (output of apply)
        return x * (1.0 - x);
    }
}
