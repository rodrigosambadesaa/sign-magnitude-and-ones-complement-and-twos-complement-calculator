# Arbitrary-Precision Sign-Magnitude / One's Complement / Two's Complement Calculator

Java 17 + Maven + Eclipse calculator for arbitrary-precision real and complex arithmetic with support for:

- Sign-magnitude
- One's complement
- Two's complement

The mathematical engine uses `BigDecimal` and `BigComplex` from **big-math 2.3.2**. Calculator operations and binary encoding do not convert values to `double`.

## Features

### Arbitrary-precision arithmetic

Real values use `BigDecimal`; complex values use `BigComplex` with arbitrary-precision real and imaginary components.

Supported operations include:

- `add`, `sub`, `mul`, `div`
- `pow`, `root`, `sqrt`
- `exp`, `log`
- `sin`, `cos`, `tan`
- `asin`, `acos`, `atan`
- `factorial`, `gamma`
- `conjugate`, `negate`, `reciprocal`

Precision is selected per calculation through `MathContext`. The project does not impose a fixed decimal-precision ceiling; practical limits are memory and execution time.

### Signed binary formats

`BinaryCodec` converts arbitrary-size `BigDecimal` values to and from fixed-width signed binary representations.

For decimal values, the representation is fixed-point:

```text
scaledInteger = decimalValue * 2^fractionalBits
```

If a decimal value is not exactly representable with the selected number of fractional bits, the default rounding mode is `HALF_EVEN`. Total width and fractional-bit count are configurable.

The codec also supports:

- automatic calculation of the minimum representable width;
- conversion between sign-magnitude, one's complement and two's complement;
- explicit preservation of negative zero when the source and destination formats support it;
- overflow detection instead of silent truncation.

Complex values are encoded by applying the selected representation independently to their real and imaginary components.

## Command-line interface

```text
calc <op> <aReal> <aImag> [bReal bImag] <precision>

calc-binary <op> <aRealBits> <aImagBits> [bRealBits bImagBits]
            <sm|ones|twos> <width> <fractionBits> <precision>

encode <decimal> <sm|ones|twos> <width> <fractionBits>

encode-min <decimal> <sm|ones|twos> <fractionBits>

decode <bits> <sm|ones|twos> <width> <fractionBits>

convert <bits> <sourceEncoding> <targetEncoding> <width> <fractionBits>

encode-complex <real> <imag> <sm|ones|twos> <width> <fractionBits>
```

Accepted encoding aliases include `sm`, `ones` / `c1`, and `twos` / `c2`.

## Representation ranges

For a width of `N` bits after fixed-point scaling:

| Encoding | Minimum scaled integer | Maximum scaled integer |
|---|---:|---:|
| Sign-magnitude | `-(2^(N-1)-1)` | `2^(N-1)-1` |
| One's complement | `-(2^(N-1)-1)` | `2^(N-1)-1` |
| Two's complement | `-2^(N-1)` | `2^(N-1)-1` |

Sign-magnitude and one's complement each have two zero bit patterns. The detailed decoder preserves whether the negative-zero pattern was used; two's complement has a single canonical zero.

## Import into Eclipse

1. **File > Import > Maven > Existing Maven Projects**
2. Select the repository directory.
3. Use a Java 17 JDK.
4. Eclipse/m2e will resolve Maven dependencies automatically.

The repository includes `.project`, `.classpath`, and Java compiler settings.

## Build and test

```bash
mvn clean verify
```

GitHub Actions runs the same verification on pushes and pull requests to `main`.

## Design

The project separates:

1. **Mathematical values** — arbitrary-precision decimal and complex arithmetic.
2. **Machine representation** — finite-width signed binary fixed-point encoding.

This distinction is necessary because arbitrary decimal precision does not imply that every decimal has a finite binary expansion, while complement representations necessarily use a selected finite width.
