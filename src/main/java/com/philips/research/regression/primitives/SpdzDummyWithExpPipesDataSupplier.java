package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.MultiplicationTripleShares;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

public class SpdzDummyWithExpPipesDataSupplier implements SpdzDataSupplier {

    private final int myId;
    private final ArithmeticDummyDataSupplier supplier;
    private final FieldDefinition fieldDefinition;
    private final BigInteger wholeKey;
    private final BigInteger myKeyShare;
    private final Function<Integer, SpdzSInt[]> preprocessedValues;


    public SpdzDummyWithExpPipesDataSupplier(int myId, int noOfPlayers,
        FieldDefinition fieldDefinition,
        Function<Integer, SpdzSInt[]> preprocessedValues) {
        this(myId, noOfPlayers, fieldDefinition, preprocessedValues,
            createKeyPair(myId, noOfPlayers, fieldDefinition.getModulus()));
    }

    public SpdzDummyWithExpPipesDataSupplier(int myId, int noOfPlayers,
        FieldDefinition fieldDefinition,
        Function<Integer, SpdzSInt[]> preprocessedValues,
        Pair<BigInteger, BigInteger> keyPair) {
        this.myId = myId;
        this.fieldDefinition = fieldDefinition;
        this.supplier =
            new ArithmeticDummyDataSupplier(myId, noOfPlayers, fieldDefinition.getModulus());
        this.wholeKey = keyPair.getFirst();
        this.myKeyShare = keyPair.getSecond();
        this.preprocessedValues = preprocessedValues;
    }

    public static Pair<BigInteger, BigInteger> createKeyPair(int myId, int noOfPlayers,
        BigInteger modulus) {
        return new ArithmeticDummyDataSupplier(myId, noOfPlayers, modulus).getRandomElementShare();
    }

    @Override
    public SpdzTriple getNextTriple() {
        MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
        return new SpdzTriple(
            toSpdzSInt(rawTriple.getLeft()),
            toSpdzSInt(rawTriple.getRight()),
            toSpdzSInt(rawTriple.getProduct()));
    }

    @Override
    public SpdzSInt[] getNextExpPipe() {
//        List<Pair<BigInteger, BigInteger>> rawExpPipe = supplier.getExpPipe(200);
//        return rawExpPipe.stream()
//            .map(this::toSpdzSInt)
//            .toArray(SpdzSInt[]::new);
        return this.preprocessedValues.apply(200);
    }

    @Override
    public SpdzInputMask getNextInputMask(int towardPlayerId) {
        Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
        if (myId == towardPlayerId) {
            return new SpdzInputMask(toSpdzSInt(raw), createElement(raw.getFirst()));
        } else {
            return new SpdzInputMask(toSpdzSInt(raw), null);
        }
    }

    @Override
    public SpdzSInt getNextBit() {
        return toSpdzSInt(supplier.getRandomBitShare());
    }

    @Override
    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    @Override
    public FieldElement getSecretSharedKey() {
        return createElement(myKeyShare);
    }

    @Override
    public SpdzSInt getNextRandomFieldElement() {
        return toSpdzSInt(supplier.getRandomElementShare());
    }

    private SpdzSInt toSpdzSInt(Pair<BigInteger, BigInteger> raw) {
        return new SpdzSInt(
            fieldDefinition.createElement(raw.getSecond()),
            fieldDefinition.createElement(raw.getSecond().multiply(wholeKey))
        );
    }

    private FieldElement createElement(BigInteger value) {
        return fieldDefinition.createElement(value);
    }
}
