package dev.rodrigosambade.calculator.math;

import java.math.MathContext;

import ch.obermuhlner.math.big.BigComplex;
import dev.rodrigosambade.calculator.encoding.BinaryFormat;
import dev.rodrigosambade.calculator.encoding.EncodedComplex;
import dev.rodrigosambade.calculator.encoding.SignedEncoding;

public final class EncodedCalculator {
    private final ArbitraryPrecisionCalculator calculator;
    private final SignedEncoding encoding;
    private final BinaryFormat format;

    public EncodedCalculator(MathContext mathContext, SignedEncoding encoding, BinaryFormat format) {
        this.calculator = new ArbitraryPrecisionCalculator(mathContext);
        this.encoding = encoding;
        this.format = format;
    }

    public EncodedComplex calculate(String operation, EncodedComplex a, EncodedComplex b) {
        BigComplex decodedA = a.decode(encoding, format);
        BigComplex decodedB = b == null ? null : b.decode(encoding, format);
        BigComplex result = calculator.calculate(operation, decodedA, decodedB);
        return EncodedComplex.encode(result, encoding, format);
    }
}
