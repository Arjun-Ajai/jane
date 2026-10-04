package com.jane.predict;

import java.util.List;

public interface Dataset {
    int size();
    double[] inputsFor(int index);
    double[] targetsFor(int index);
    List<Integer> shuffledIndices();
}
