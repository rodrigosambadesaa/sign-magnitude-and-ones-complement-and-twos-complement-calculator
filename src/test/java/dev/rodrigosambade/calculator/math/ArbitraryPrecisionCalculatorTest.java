package dev.rodrigosambade.calculator.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;
import ch.obermuhlner.math.big.BigComplex;

class ArbitraryPrecisionCalculatorTest {
    private final MathContext mc = new MathContext(80);
    private final ArbitraryPrecisionCalculator calc = new ArbitraryPrecisionCalculator(mc);

    @Test
    void addsComplexNumbersExactlyAtRequestedPrecision() {
        BigComplex a = BigComplex.valueOf(new BigDecimal("1.25"), new BigDecimal("2.5"));
        BigComplex b = BigComplex.valueOf(new BigDecimal("-3"), new BigDecimal("0.125"));
        BigComplex r = calc.calculate("add", a, b);
        assertEquals(0, new BigDecimal("-1.75").compareTo(r.re));
        assertEquals(0, new BigDecimal("2.625").compareTo(r.im));
    }

    @Test
    void squareRootOfMinusOneIsImaginaryUnit() {
        BigComplex r = calc.calculate("sqrt", BigComplex.valueOf(new BigDecimal("-1")), null);
        assertTrue(r.re.abs().compareTo(new BigDecimal("1E-70")) < 0);
        assertTrue(r.im.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("1E-70")) < 0);
    }

    @Test
    void arbitraryPrecisionRetainsManyDigits() {
        BigComplex one = BigComplex.valueOf(BigDecimal.ONE);
        BigComplex three = BigComplex.valueOf(new BigDecimal("3"));
        BigComplex r = calc.calculate("div", one, three);
        assertTrue(r.re.toPlainString().startsWith("0.333333333333333333333333333333333333"));
    }
}
