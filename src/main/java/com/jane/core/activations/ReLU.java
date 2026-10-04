package com.jane.core.activations;

import com.jane.core.ActivationFunction;

public class ReLU implements ActivationFunction {
    @Override
    public double apply(double x) {
        return Math.max(0, x);
    }

    @Override
    public double derivative(double x) {
        // x here is assumed to be the already-activated value (output of apply)
        return x > 0 ? 1 : 0;
    }
}
