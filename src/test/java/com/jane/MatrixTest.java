package com.jane;

import com.jane.core.Matrix;
import com.jane.core.exceptions.DimensionMismatchException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixTest {

    @Test
    void testAdd() {
        Matrix m1 = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix m2 = new Matrix(new double[][]{{5, 6}, {7, 8}});
        Matrix result = m1.add(m2);
        
        assertArrayEquals(new double[]{6, 8, 10, 12}, result.toArray(), 0.001);
    }

    @Test
    void testSubtract() {
        Matrix m1 = new Matrix(new double[][]{{5, 6}, {7, 8}});
        Matrix m2 = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix result = m1.subtract(m2);
        
        assertArrayEquals(new double[]{4, 4, 4, 4}, result.toArray(), 0.001);
    }

    @Test
    void testMultiplyMatrix() {
        Matrix m1 = new Matrix(new double[][]{{1, 2, 3}, {4, 5, 6}}); // 2x3
        Matrix m2 = new Matrix(new double[][]{{7, 8}, {9, 10}, {11, 12}}); // 3x2
        Matrix result = m1.multiply(m2); // Should be 2x2
        
        assertEquals(2, result.rows);
        assertEquals(2, result.cols);
        assertArrayEquals(new double[]{58, 64, 139, 154}, result.toArray(), 0.001);
    }

    @Test
    void testMultiplyElementwise() {
        Matrix m1 = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix m2 = new Matrix(new double[][]{{2, 3}, {4, 5}});
        Matrix result = m1.multiplyElementwise(m2);
        
        assertArrayEquals(new double[]{2, 6, 12, 20}, result.toArray(), 0.001);
    }

    @Test
    void testTranspose() {
        Matrix m1 = new Matrix(new double[][]{{1, 2, 3}, {4, 5, 6}});
        Matrix result = m1.transpose();
        
        assertEquals(3, result.rows);
        assertEquals(2, result.cols);
        assertArrayEquals(new double[]{1, 4, 2, 5, 3, 6}, result.toArray(), 0.001);
    }

    @Test
    void testDimensionMismatch() {
        Matrix m1 = new Matrix(2, 2);
        Matrix m2 = new Matrix(3, 3);
        assertThrows(DimensionMismatchException.class, () -> m1.add(m2));
        assertThrows(DimensionMismatchException.class, () -> m1.multiply(m2));
    }
}
