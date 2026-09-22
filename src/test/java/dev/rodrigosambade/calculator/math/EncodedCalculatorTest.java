package dev.rodrigosambade.calculator.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.MathContext;
import org.junit.jupiter.api.Test;

import dev.rodrigosambade.calculator.encoding.BinaryFormat;
import dev.rodrigosambade.calculator.encoding.EncodedComplex;
import dev.rodrigosambade.calculator.encoding.SignedEncoding;

class EncodedCalculatorTest {
    @Test
    void calculatesDirectlyFromTwosComplementOperands() {
        BinaryFormat format = new BinaryFormat(16, 4);
        EncodedCalculator calc = new EncodedCalculator(
                new MathContext(80), SignedEncoding.TWOS_COMPLEMENT, format);

        EncodedComplex a = new EncodedComplex("0000000000010100", "0000000000101000"); // 1.25 + 2.5i
        EncodedComplex b = new EncodedComplex("1111111111010000", "0000000000000010"); // -3 + 0.125i

        EncodedComplex result = calc.calculate("add", a, b);

        assertEquals("1111111111100100", result.realBits());      // -1.75
        assertEquals("0000000000101010", result.imaginaryBits()); // 2.625
    }
}
