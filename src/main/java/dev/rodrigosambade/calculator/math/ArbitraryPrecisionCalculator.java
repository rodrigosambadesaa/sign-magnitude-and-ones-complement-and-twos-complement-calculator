package dev.rodrigosambade.calculator.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import ch.obermuhlner.math.big.BigComplex;
import ch.obermuhlner.math.big.BigComplexMath;

public final class ArbitraryPrecisionCalculator {
    private final MathContext mathContext;

    public ArbitraryPrecisionCalculator(MathContext mathContext) {
        if (mathContext == null || mathContext.getPrecision() <= 0) {
            throw new IllegalArgumentException("A finite positive MathContext precision is required");
        }
        this.mathContext = mathContext;
    }

    public MathContext mathContext() {
        return mathContext;
    }

    public BigComplex calculate(String operation, BigComplex a, BigComplex b) {
        String op = operation.toLowerCase(Locale.ROOT);
        return switch (op) {
            case "add", "+" -> a.add(requireB(b), mathContext);
            case "sub", "subtract", "-" -> a.subtract(requireB(b), mathContext);
            case "mul", "multiply", "*" -> a.multiply(requireB(b), mathContext);
            case "div", "divide", "/" -> a.divide(requireB(b), mathContext);
            case "pow", "^" -> BigComplexMath.pow(a, requireB(b), mathContext);
            case "root" -> BigComplexMath.root(a, requireB(b), mathContext);
            case "sqrt" -> BigComplexMath.sqrt(a, mathContext);
            case "exp" -> BigComplexMath.exp(a, mathContext);
            case "log", "ln" -> BigComplexMath.log(a, mathContext);
            case "sin" -> BigComplexMath.sin(a, mathContext);
            case "cos" -> BigComplexMath.cos(a, mathContext);
            case "tan" -> BigComplexMath.tan(a, mathContext);
            case "asin" -> BigComplexMath.asin(a, mathContext);
            case "acos" -> BigComplexMath.acos(a, mathContext);
            case "atan" -> BigComplexMath.atan(a, mathContext);
            case "factorial", "fact" -> BigComplexMath.factorial(a, mathContext);
            case "gamma" -> BigComplexMath.gamma(a, mathContext);
            case "conjugate", "conj" -> a.conjugate();
            case "negate", "neg" -> a.negate();
            case "reciprocal", "inv" -> a.reciprocal(mathContext);
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        };
    }

    public BigDecimal abs(BigComplex value) {
        return value.abs(mathContext);
    }

    public BigDecimal angle(BigComplex value) {
        return value.angle(mathContext);
    }

    private static BigComplex requireB(BigComplex b) {
        if (b == null) throw new IllegalArgumentException("This operation requires a second operand");
        return b;
    }
}
