package dev.rodrigosambade.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.obermuhlner.math.big.BigComplex;
import dev.rodrigosambade.calculator.encoding.BinaryCodec;
import dev.rodrigosambade.calculator.encoding.BinaryFormat;
import dev.rodrigosambade.calculator.encoding.DecodedBinaryValue;
import dev.rodrigosambade.calculator.encoding.EncodedComplex;
import dev.rodrigosambade.calculator.encoding.SignedEncoding;
import dev.rodrigosambade.calculator.math.ArbitraryPrecisionCalculator;
import dev.rodrigosambade.calculator.math.ComplexParser;
import dev.rodrigosambade.calculator.math.EncodedCalculator;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printHelp();
                return;
            }
            switch (args[0].toLowerCase()) {
                case "calc" -> calculate(args);
                case "calc-binary" -> calculateBinary(args);
                case "encode" -> encode(args);
                case "encode-min" -> encodeMinimum(args);
                case "decode" -> decode(args);
                case "convert" -> convert(args);
                case "encode-complex" -> encodeComplex(args);
                default -> printHelp();
            }
        } catch (RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void calculate(String[] args) {
        if (args.length < 5) {
            throw new IllegalArgumentException("calc <operation> <aReal> <aImag> [bReal bImag] <precision>");
        }
        String operation = args[1];
        BigComplex a = ComplexParser.of(args[2], args[3]);
        boolean binary = requiresSecondOperand(operation);
        int precisionIndex = binary ? 6 : 4;
        if (binary && args.length < 7) {
            throw new IllegalArgumentException("Operation " + operation + " requires bReal and bImag");
        }
        int precision = Integer.parseInt(args[precisionIndex]);
        BigComplex b = binary ? ComplexParser.of(args[4], args[5]) : null;
        ArbitraryPrecisionCalculator calculator =
                new ArbitraryPrecisionCalculator(new MathContext(precision, RoundingMode.HALF_EVEN));
        System.out.println(format(calculator.calculate(operation, a, b)));
    }

    private static void calculateBinary(String[] args) {
        if (args.length < 8) {
            throw new IllegalArgumentException(
                    "calc-binary <operation> <aRealBits> <aImagBits> [bRealBits bImagBits] "
                    + "<sm|ones|twos> <width> <fractionBits> <precision>");
        }
        String operation = args[1];
        boolean binary = requiresSecondOperand(operation);
        if (binary && args.length != 10) {
            throw new IllegalArgumentException("Binary operation " + operation + " requires two complex operands");
        }
        if (!binary && args.length != 8) {
            throw new IllegalArgumentException("Unary operation " + operation + " requires one complex operand");
        }

        int configIndex = binary ? 6 : 4;
        SignedEncoding encoding = SignedEncoding.parse(args[configIndex]);
        BinaryFormat format = new BinaryFormat(
                Integer.parseInt(args[configIndex + 1]),
                Integer.parseInt(args[configIndex + 2]));
        int precision = Integer.parseInt(args[configIndex + 3]);

        EncodedComplex a = new EncodedComplex(args[2], args[3]);
        EncodedComplex b = binary ? new EncodedComplex(args[4], args[5]) : null;
        EncodedCalculator calculator = new EncodedCalculator(
                new MathContext(precision, RoundingMode.HALF_EVEN), encoding, format);
        EncodedComplex result = calculator.calculate(operation, a, b);
        System.out.println("real=" + BinaryCodec.withBinaryPoint(result.realBits(), format.fractionalBits()));
        System.out.println("imag=" + BinaryCodec.withBinaryPoint(result.imaginaryBits(), format.fractionalBits()));
    }

    private static void encode(String[] args) {
        if (args.length != 5) {
            throw new IllegalArgumentException("encode <decimal> <sm|ones|twos> <width> <fractionBits>");
        }
        BigDecimal value = new BigDecimal(args[1]);
        SignedEncoding encoding = SignedEncoding.parse(args[2]);
        BinaryFormat format = new BinaryFormat(Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        String bits = BinaryCodec.encode(value, encoding, format);
        System.out.println(BinaryCodec.withBinaryPoint(bits, format.fractionalBits()));
    }

    private static void encodeMinimum(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("encode-min <decimal> <sm|ones|twos> <fractionBits>");
        }
        BigDecimal value = new BigDecimal(args[1]);
        SignedEncoding encoding = SignedEncoding.parse(args[2]);
        int fractionalBits = Integer.parseInt(args[3]);
        int width = BinaryCodec.minimumWidth(value, encoding, fractionalBits);
        String bits = BinaryCodec.encodeMinimum(value, encoding, fractionalBits);
        System.out.println("width=" + width);
        System.out.println("bits=" + BinaryCodec.withBinaryPoint(bits, fractionalBits));
    }

    private static void decode(String[] args) {
        if (args.length != 5) {
            throw new IllegalArgumentException("decode <bits> <sm|ones|twos> <width> <fractionBits>");
        }
        SignedEncoding encoding = SignedEncoding.parse(args[2]);
        BinaryFormat format = new BinaryFormat(Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        DecodedBinaryValue decoded = BinaryCodec.decodeDetailed(args[1], encoding, format);
        System.out.println(decoded.negativeZero() ? "-0" : decoded.value().toPlainString());
    }

    private static void convert(String[] args) {
        if (args.length != 6) {
            throw new IllegalArgumentException(
                    "convert <bits> <sourceEncoding> <targetEncoding> <width> <fractionBits>");
        }
        SignedEncoding source = SignedEncoding.parse(args[2]);
        SignedEncoding target = SignedEncoding.parse(args[3]);
        BinaryFormat format = new BinaryFormat(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        String bits = BinaryCodec.convert(args[1], source, target, format);
        System.out.println(BinaryCodec.withBinaryPoint(bits, format.fractionalBits()));
    }

    private static void encodeComplex(String[] args) {
        if (args.length != 6) {
            throw new IllegalArgumentException("encode-complex <real> <imag> <sm|ones|twos> <width> <fractionBits>");
        }
        BigComplex value = ComplexParser.of(args[1], args[2]);
        SignedEncoding encoding = SignedEncoding.parse(args[3]);
        BinaryFormat format = new BinaryFormat(Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        EncodedComplex encoded = EncodedComplex.encode(value, encoding, format);
        System.out.println("real=" + BinaryCodec.withBinaryPoint(encoded.realBits(), format.fractionalBits()));
        System.out.println("imag=" + BinaryCodec.withBinaryPoint(encoded.imaginaryBits(), format.fractionalBits()));
    }

    private static boolean requiresSecondOperand(String op) {
        return switch (op.toLowerCase()) {
            case "add", "+", "sub", "subtract", "-", "mul", "multiply", "*",
                 "div", "divide", "/", "pow", "^", "root" -> true;
            default -> false;
        };
    }

    private static String format(BigComplex value) {
        String sign = value.im.signum() < 0 ? " - " : " + ";
        return value.re.stripTrailingZeros().toPlainString() + sign
                + value.im.abs().stripTrailingZeros().toPlainString() + "i";
    }

    private static void printHelp() {
        System.out.println("""
            Arbitrary Precision Signed Binary Calculator

            calc <op> <aReal> <aImag> [bReal bImag] <precision>
            calc-binary <op> <aRealBits> <aImagBits> [bRealBits bImagBits]
                        <sm|ones|twos> <width> <fractionBits> <precision>

              ops: add sub mul div pow root sqrt exp log sin cos tan asin acos atan
                   factorial gamma conjugate negate reciprocal

            encode <decimal> <sm|ones|twos> <width> <fractionBits>
            encode-min <decimal> <sm|ones|twos> <fractionBits>
            decode <bits>    <sm|ones|twos> <width> <fractionBits>
            convert <bits> <sourceEncoding> <targetEncoding> <width> <fractionBits>
            encode-complex <real> <imag> <sm|ones|twos> <width> <fractionBits>

            Precision is arbitrary but finite per calculation. Binary decimal encodings use
            fixed-point scaling with configurable fractional bits and HALF_EVEN rounding.
            """);
    }
}
