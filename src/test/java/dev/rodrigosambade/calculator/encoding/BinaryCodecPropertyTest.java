package dev.rodrigosambade.calculator.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class BinaryCodecPropertyTest {

    @Test
    void everyRepresentableIntegerRoundTripsAcrossSmallWidths() {
        for (SignedEncoding encoding : SignedEncoding.values()) {
            for (int width = 2; width <= 10; width++) {
                BinaryFormat format = new BinaryFormat(width, 0);
                BigInteger half = BigInteger.ONE.shiftLeft(width - 1);
                BigInteger max = half.subtract(BigInteger.ONE);
                BigInteger min = encoding == SignedEncoding.TWOS_COMPLEMENT
                        ? half.negate()
                        : max.negate();

                for (BigInteger value = min;
                        value.compareTo(max) <= 0;
                        value = value.add(BigInteger.ONE)) {
                    BigDecimal decimal = new BigDecimal(value);
                    String bits = BinaryCodec.encode(decimal, encoding, format);
                    assertEquals(decimal, BinaryCodec.decode(bits, encoding, format));
                }
            }
        }
    }

    @Test
    void valuesImmediatelyOutsideEachRangeAreRejected() {
        for (SignedEncoding encoding : SignedEncoding.values()) {
            for (int width = 2; width <= 10; width++) {
                BinaryFormat format = new BinaryFormat(width, 0);
                BigInteger half = BigInteger.ONE.shiftLeft(width - 1);
                BigInteger max = half.subtract(BigInteger.ONE);
                BigInteger min = encoding == SignedEncoding.TWOS_COMPLEMENT
                        ? half.negate()
                        : max.negate();

                assertThrows(ArithmeticException.class,
                        () -> BinaryCodec.encode(new BigDecimal(max.add(BigInteger.ONE)), encoding, format));
                assertThrows(ArithmeticException.class,
                        () -> BinaryCodec.encode(new BigDecimal(min.subtract(BigInteger.ONE)), encoding, format));
            }
        }
    }

    @Test
    void minimumWidthAlwaysFitsAndOneBitLessDoesNotWhenARealReductionExists() {
        for (SignedEncoding encoding : SignedEncoding.values()) {
            for (int magnitude = 0; magnitude <= 40; magnitude++) {
                for (int sign : new int[] { -1, 1 }) {
                    BigDecimal value = BigDecimal.valueOf((long) magnitude * sign);
                    int width = BinaryCodec.minimumWidth(value, encoding, 0);
                    BinaryFormat format = new BinaryFormat(width, 0);

                    String bits = BinaryCodec.encode(value, encoding, format);
                    assertEquals(width, bits.length());
                    assertEquals(value, BinaryCodec.decode(bits, encoding, format));

                    if (width > 1) {
                        BinaryFormat smaller = new BinaryFormat(width - 1, 0);
                        assertThrows(ArithmeticException.class,
                                () -> BinaryCodec.encode(value, encoding, smaller));
                    }
                }
            }
        }
    }

    @Test
    void negativeZeroMetadataIsPreservedWhereSupported() {
        for (int width = 2; width <= 10; width++) {
            BinaryFormat format = new BinaryFormat(width, 0);

            String signMagnitudeNegativeZero =
                    BinaryCodec.encodeNegativeZero(SignedEncoding.SIGN_MAGNITUDE, format);
            String onesComplementNegativeZero =
                    BinaryCodec.encodeNegativeZero(SignedEncoding.ONES_COMPLEMENT, format);

            assertTrue(BinaryCodec.decodeDetailed(
                    signMagnitudeNegativeZero, SignedEncoding.SIGN_MAGNITUDE, format).negativeZero());
            assertTrue(BinaryCodec.decodeDetailed(
                    onesComplementNegativeZero, SignedEncoding.ONES_COMPLEMENT, format).negativeZero());

            String converted = BinaryCodec.convert(
                    onesComplementNegativeZero,
                    SignedEncoding.ONES_COMPLEMENT,
                    SignedEncoding.SIGN_MAGNITUDE,
                    format);
            assertTrue(BinaryCodec.decodeDetailed(
                    converted, SignedEncoding.SIGN_MAGNITUDE, format).negativeZero());

            String twos = BinaryCodec.convert(
                    onesComplementNegativeZero,
                    SignedEncoding.ONES_COMPLEMENT,
                    SignedEncoding.TWOS_COMPLEMENT,
                    format);
            assertFalse(BinaryCodec.decodeDetailed(
                    twos, SignedEncoding.TWOS_COMPLEMENT, format).negativeZero());
            assertEquals(BigDecimal.ZERO,
                    BinaryCodec.decode(twos, SignedEncoding.TWOS_COMPLEMENT, format));
        }
    }
}
