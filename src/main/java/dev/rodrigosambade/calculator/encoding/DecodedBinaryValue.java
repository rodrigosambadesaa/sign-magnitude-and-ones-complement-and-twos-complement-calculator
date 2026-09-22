package dev.rodrigosambade.calculator.encoding;

import java.math.BigDecimal;

/**
 * Decoded signed-binary value. BigDecimal itself cannot distinguish +0 from -0,
 * so negativeZero preserves that bit-pattern information for conversions.
 */
public record DecodedBinaryValue(BigDecimal value, boolean negativeZero) {
    public DecodedBinaryValue {
        if (value == null) throw new IllegalArgumentException("value is required");
        if (negativeZero && value.signum() != 0) {
            throw new IllegalArgumentException("negativeZero is only valid for numeric zero");
        }
    }
}
