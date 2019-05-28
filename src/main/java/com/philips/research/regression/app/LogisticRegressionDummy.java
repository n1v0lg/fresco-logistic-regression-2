package com.philips.research.regression.app;

import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.VectorUtils.vectorWithNull;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

class LogisticRegressionDummy implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {

    private final int myId;
    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final double lambda;
    private final int iterations;
    private final double privacyBudget;
    private final double sensitivity;

    LogisticRegressionDummy(int myId, Matrix<BigDecimal> matrix, Vector<BigDecimal> vector,
        double lambda, int iterations, double privacyBudget, double sensitivity) {
        this.myId = myId;
        this.matrix = new MatrixWithIntercept(matrix);
        this.vector = vector;
        this.lambda = lambda;
        this.iterations = iterations;
        this.privacyBudget = privacyBudget;
        this.sensitivity = sensitivity;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
//        return builder.seq(seq -> {
//            DRes<SInt> foo = seq.numeric().input(1, 1);
//            System.out.println("foo");
//            DRes<BigInteger> opened = seq.numeric().open(foo);
//
//            return ArrayList::new;
//        });
//
        return builder.par(par -> {
            DRes<Vector<DRes<SReal>>> y1;
            RealLinearAlgebra linAlg = par.realLinAlg();
            System.out.println("Vector party " + myId + " " + vector);
            if (myId == 1) {
                System.out.println("Vector size party 1: " + vector.size());
                y1 = linAlg.input(vector, 1);
            } else {
                System.out.println("Vector size party 2: " + vector.size());
                y1 = linAlg.input(vectorWithNull(vector.size()), 1);
            }
            return y1;
        }).seq((seq, inputs) -> {
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(() -> inputs);
            return () -> unwrap(opened);
        });
    }
}

