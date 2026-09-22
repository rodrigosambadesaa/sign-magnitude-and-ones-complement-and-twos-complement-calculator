package dev.rodrigosambade.calculator.encoding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class BinaryCodec {
    private BinaryCodec() {}

    public static String encode(BigDecimal value, SignedEncoding encoding, BinaryFormat format) {
        BigInteger scaled = scale(value, format);
        validateRange(scaled, encoding, format.width());

        BigInteger encoded = switch (encoding) {
            case SIGN_MAGNITUDE -> encodeSignMagnitude(scaled, format.width());
            case ONES_COMPLEMENT -> encodeOnesComplement(scaled, format.width());
            case TWOS_COMPLEMENT -> encodeTwosComplement(scaled, format.width());
        };
        return pad(encoded.toString(2), format.width());
    }

    /**
     * Encodes numeric zero using the negative-zero bit pattern when the target
     * representation supports it. Two's complement has only one zero.
     */
    public static String encodeNegativeZero(SignedEncoding encoding, BinaryFormat format) {
        return switch (encoding) {
            case SIGN_MAGNITUDE -> "1" + "0".repeat(format.width() - 1);
            case ONES_COMPLEMENT -> "1".repeat(format.width());
            case TWOS_COMPLEMENT -> "0".repeat(format.width());
        };
    }

    public static BigDecimal decode(String bits, SignedEncoding encoding, BinaryFormat format) {
        return decodeDetailed(bits, encoding, format).value();
    }

    public static DecodedBinaryValue decodeDetailed(String bits, SignedEncoding encoding, BinaryFormat format) {
        String normalized = normalize(bits, format.width());
        BigInteger raw = new BigInteger(normalized, 2);
        boolean negativeZero = isNegativeZeroRaw(raw, encoding, format.width());

        BigInteger scaled = switch (encoding) {
            case SIGN_MAGNITUDE -> decodeSignMagnitude(raw, format.width());
            case ONES_COMPLEMENT -> decodeOnesComplement(raw, format.width());
            case TWOS_COMPLEMENT -> decodeTwosComplement(raw, format.width());
        };
        BigDecimal divisor = new BigDecimal(BigInteger.ONE.shiftLeft(format.fractionalBits()));
        return new DecodedBinaryValue(new BigDecimal(scaled).divide(divisor), negativeZero);
    }

    /**
     * Converts between signed encodings while keeping the same width and
     * fractional-bit count. Negative zero is preserved when both source and
     * target representations support it.
     */
    public static String convert(String bits, SignedEncoding source, SignedEncoding target, BinaryFormat format) {
        DecodedBinaryValue decoded = decodeDetailed(bits, source, format);
        if (decoded.negativeZero()) {
            return encodeNegativeZero(target, format);
        }
        return encode(decoded.value(), target, format);
    }

    /**
     * Returns the smallest total bit width that can represent the value after
     * fixed-point scaling with the requested fractional-bit count.
     */
    public static int minimumWidth(BigDecimal value, SignedEncoding encoding, int fractionalBits) {
        return minimumWidth(value, encoding, fractionalBits, RoundingMode.HALF_EVEN);
    }

    public static int minimumWidth(BigDecimal value, SignedEncoding encoding, int fractionalBits,
            RoundingMode roundingMode) {
        if (fractionalBits < 0) throw new IllegalArgumentException("fractionalBits must be >= 0");
        if (roundingMode == null) throw new IllegalArgumentException("roundingMode is required");

        BigDecimal factor = new BigDecimal(BigInteger.ONE.shiftLeft(fractionalBits));
        BigInteger scaled = value.multiply(factor).setScale(0, roundingMode).toBigIntegerExact();

        int integerWidth;
        if (encoding == SignedEncoding.TWOS_COMPLEMENT) {
            if (scaled.signum() >= 0) {
                integerWidth = scaled.bitLength() + 1;
            } else {
                integerWidth = scaled.negate().subtract(BigInteger.ONE).bitLength() + 1;
            }
        } else {
            integerWidth = scaled.abs().bitLength() + 1;
        }

        // At least one sign bit, plus any explicitly requested fractional bits.
        return Math.max(Math.max(1, integerWidth), fractionalBits + 1);
    }

    public static String encodeMinimum(BigDecimal value, SignedEncoding encoding, int fractionalBits) {
        BinaryFormat format = new BinaryFormat(minimumWidth(value, encoding, fractionalBits), fractionalBits);
        return encode(value, encoding, format);
    }

    public static String withBinaryPoint(String bits, int fractionalBits) {
        if (fractionalBits == 0) return bits;
        int split = bits.length() - fractionalBits;
        return bits.substring(0, split) + "." + bits.substring(split);
    }

    private static BigInteger scale(BigDecimal value, BinaryFormat format) {
        BigDecimal factor = new BigDecimal(BigInteger.ONE.shiftLeft(format.fractionalBits()));
        return value.multiply(factor).setScale(0, format.roundingMode()).toBigIntegerExact();
    }

    private static void validateRange(BigInteger value, SignedEncoding encoding, int width) {
        BigInteger half = BigInteger.ONE.shiftLeft(width - 1);
        BigInteger min;
        BigInteger max = half.subtract(BigInteger.ONE);
        if (encoding == SignedEncoding.TWOS_COMPLEMENT) {
            min = half.negate();
        } else {
            min = max.negate();
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ArithmeticException("Value does not fit in " + width + " bits for " + encoding
                    + " (scaled integer " + value + ", range " + min + ".." + max + ")");
        }
    }

    private static BigInteger encodeSignMagnitude(BigInteger value, int width) {
        if (value.signum() >= 0) return value;
        return BigInteger.ONE.shiftLeft(width - 1).or(value.abs());
    }

    private static BigInteger encodeOnesComplement(BigInteger value, int width) {
        if (value.signum() >= 0) return value;
        BigInteger mask = BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE);
        return value.abs().xor(mask);
    }

    private static BigInteger encodeTwosComplement(BigInteger value, int width) {
        return value.signum() >= 0 ? value : BigInteger.ONE.shiftLeft(width).add(value);
    }

    private static BigInteger decodeSignMagnitude(BigInteger raw, int width) {
        if (!raw.testBit(width - 1)) return raw;
        BigInteger magnitudeMask = BigInteger.ONE.shiftLeft(width - 1).subtract(BigInteger.ONE);
        return raw.and(magnitudeMask).negate();
    }

    private static BigInteger decodeOnesComplement(BigInteger raw, int width) {
        if (!raw.testBit(width - 1)) return raw;
        BigInteger mask = BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE);
        BigInteger magnitude = raw.xor(mask);
        return magnitude.signum() == 0 ? BigInteger.ZERO : magnitude.negate();
    }

    private static BigInteger decodeTwosComplement(BigInteger raw, int width) {
        return raw.testBit(width - 1) ? raw.subtract(BigInteger.ONE.shiftLeft(width)) : raw;
    }

    private static boolean isNegativeZeroRaw(BigInteger raw, SignedEncoding encoding, int width) {
        return switch (encoding) {
            case SIGN_MAGNITUDE -> raw.equals(BigInteger.ONE.shiftLeft(width - 1));
            case ONES_COMPLEMENT -> raw.equals(BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE));
            case TWOS_COMPLEMENT -> false;
        };
    }

    private static String normalize(String bits, int width) {
        String normalized = bits.replace("_", "").replace(" ", "").replace(".", "");
        if (!normalized.matches("[01]+")) throw new IllegalArgumentException("Binary value must contain only 0 and 1");
        if (normalized.length() != width) {
            throw new IllegalArgumentException("Expected exactly " + width + " bits, got " + normalized.length());
        }
        return normalized;
    }

    private static String pad(String bits, int width) {
        return "0".repeat(width - bits.length()) + bits;
    }
}
