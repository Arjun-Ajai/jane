package com.jane;

import com.jane.core.NeuralNetwork;
import com.jane.core.activations.Sigmoid;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NeuralNetworkTest {

    @Test
    void testPredictForwardPass() {
        NeuralNetwork nn = new NeuralNetwork(2, 4, 1, new Sigmoid());
        double[] inputs = {0.5, 0.8};
        double[] output = nn.predict(inputs);
        
        assertEquals(1, output.length);
        // Sigmoid output is always between 0 and 1
        assertTrue(output[0] >= 0.0 && output[0] <= 1.0);
    }

    @Test
    void testCopy() {
        NeuralNetwork nn = new NeuralNetwork(2, 4, 1, new Sigmoid());
        NeuralNetwork copy = nn.copy();
        
        // Verify weights are deeply copied
        assertArrayEquals(nn.getWeightsIH().toArray(), copy.getWeightsIH().toArray(), 0.0);
        
        // Mutate original and ensure copy doesn't change
        nn.getWeightsIH().data[0][0] = 99.9;
        assertNotEquals(nn.getWeightsIH().data[0][0], copy.getWeightsIH().data[0][0]);
    }
}
