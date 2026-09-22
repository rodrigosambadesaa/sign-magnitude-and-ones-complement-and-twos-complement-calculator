package dev.rodrigosambade.calculator.encoding;

import ch.obermuhlner.math.big.BigComplex;

public record EncodedComplex(String realBits, String imaginaryBits) {
    public static EncodedComplex encode(BigComplex value, SignedEncoding encoding, BinaryFormat format) {
        return new EncodedComplex(
                BinaryCodec.encode(value.re, encoding, format),
                BinaryCodec.encode(value.im, encoding, format));
    }

    public BigComplex decode(SignedEncoding encoding, BinaryFormat format) {
        return BigComplex.valueOf(
                BinaryCodec.decode(realBits, encoding, format),
                BinaryCodec.decode(imaginaryBits, encoding, format));
    }
}
