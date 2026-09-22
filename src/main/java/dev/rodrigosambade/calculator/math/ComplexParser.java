package dev.rodrigosambade.calculator.math;

import java.math.BigDecimal;
import ch.obermuhlner.math.big.BigComplex;

public final class ComplexParser {
    private ComplexParser() {}

    public static BigComplex of(String real, String imaginary) {
        return BigComplex.valueOf(new BigDecimal(real), new BigDecimal(imaginary));
    }
}
