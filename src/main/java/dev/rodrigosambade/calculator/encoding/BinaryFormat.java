package dev.rodrigosambade.calculator.encoding;

import java.math.RoundingMode;

public record BinaryFormat(int width, int fractionalBits, RoundingMode roundingMode) {
    public BinaryFormat {
        if (width < 1) throw new IllegalArgumentException("width must be >= 1");
        if (fractionalBits < 0 || fractionalBits >= width) {
            throw new IllegalArgumentException("fractionalBits must be in [0, width)");
        }
        if (roundingMode == null) throw new IllegalArgumentException("roundingMode is required");
    }

    public BinaryFormat(int width, int fractionalBits) {
        this(width, fractionalBits, RoundingMode.HALF_EVEN);
    }
}
