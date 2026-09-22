package dev.rodrigosambade.calculator.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BinaryCodecTest {
    private final BinaryFormat int8 = new BinaryFormat(8, 0);

    @Test
    void encodesNegativeValueInAllThreeRepresentations() {
        assertEquals("10000110", BinaryCodec.encode(new BigDecimal("-6"), SignedEncoding.SIGN_MAGNITUDE, int8));
        assertEquals("11111001", BinaryCodec.encode(new BigDecimal("-6"), SignedEncoding.ONES_COMPLEMENT, int8));
        assertEquals("11111010", BinaryCodec.encode(new BigDecimal("-6"), SignedEncoding.TWOS_COMPLEMENT, int8));
    }

    @Test
    void roundTripsFixedPointDecimals() {
        BinaryFormat q = new BinaryFormat(16, 8);
        for (SignedEncoding encoding : SignedEncoding.values()) {
            String bits = BinaryCodec.encode(new BigDecimal("-6.25"), encoding, q);
            assertEquals(0, new BigDecimal("-6.25").compareTo(BinaryCodec.decode(bits, encoding, q)));
        }
    }

    @Test
    void supportsTwosComplementMinimumValue() {
        assertEquals("10000000", BinaryCodec.encode(new BigDecimal("-128"), SignedEncoding.TWOS_COMPLEMENT, int8));
        assertEquals(new BigDecimal("-128"), BinaryCodec.decode("10000000", SignedEncoding.TWOS_COMPLEMENT, int8));
    }

    @Test
    void rejectsOverflow() {
        assertThrows(ArithmeticException.class,
                () -> BinaryCodec.encode(new BigDecimal("128"), SignedEncoding.TWOS_COMPLEMENT, int8));
        assertThrows(ArithmeticException.class,
                () -> BinaryCodec.encode(new BigDecimal("-128"), SignedEncoding.ONES_COMPLEMENT, int8));
    }
}
