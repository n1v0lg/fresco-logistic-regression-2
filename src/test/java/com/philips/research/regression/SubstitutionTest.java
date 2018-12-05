package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static com.philips.research.regression.VectorAssert.assertEquals;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

@DisplayName("Forward and Backward Substitution")
class SubstitutionTest {

    private Runner<Vector<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("performs forward substitution")
    void forwardSubstitution() {
        Matrix<BigDecimal> L = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(0.0), valueOf(0.0)},
            {valueOf(-2.0), valueOf(1.0), valueOf(0.0)},
            {valueOf(1.0), valueOf(6.0), valueOf(1.0)}
        });
        Vector<BigDecimal> b  = new Vector<>(asList(valueOf(2.0), valueOf(-1.0), valueOf(4.0)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(2.0), valueOf(3.0), valueOf(-16.0)));
        assertEquals(expected, runner.run(new Substitution(L, b, ForwardSubstitution::new)), 3);
    }

    @Test
    @DisplayName("performs back substitution")
    void backSubstitution() {
        Matrix<BigDecimal> L = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(-2.0), valueOf(1.0)},
            {valueOf(0.0), valueOf(1.0), valueOf(6.0)},
            {valueOf(0.0), valueOf(0.0), valueOf(1.0)}
        });
        Vector<BigDecimal> b  = new Vector<>(asList(valueOf(4.0), valueOf(-1.0), valueOf(2.0)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-24.0), valueOf(-13.0), valueOf(2.0)));
        assertEquals(expected, runner.run(new Substitution(L, b, BackSubstitution::new)), 3);
    }
}

class Substitution implements Application<Vector<BigDecimal>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final Transformation transformation;

    Substitution(Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, Transformation transformation) {
        this.matrix = matrix;
        this.vector = vector;
        this.transformation = transformation;
    }

    @Override
    public DRes<Vector<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedMatrix;
        DRes<Vector<DRes<SReal>>> closedVector, closedResult;
        RealLinearAlgebra real = builder.realLinAlg();
        closedMatrix = real.input(matrix, 1);
        closedVector = real.input(vector, 1);
        closedResult = builder.seq(transformation.transform(closedMatrix, closedVector));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);
        return () -> new VectorUtils().unwrapVector(opened);
    }

    interface Transformation {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(
            DRes<Matrix<DRes<SReal>>> matrix,
            DRes<Vector<DRes<SReal>>> vector
        );
    }
}