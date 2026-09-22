package dev.rodrigosambade.calculator.encoding;

public enum SignedEncoding {
    SIGN_MAGNITUDE,
    ONES_COMPLEMENT,
    TWOS_COMPLEMENT;

    public static SignedEncoding parse(String value) {
        return switch (value.trim().toLowerCase().replace('-', '_')) {
            case "sm", "sign", "sign_magnitude", "signmagnitude" -> SIGN_MAGNITUDE;
            case "ones", "ones_complement", "c1", "1c" -> ONES_COMPLEMENT;
            case "twos", "twos_complement", "c2", "2c" -> TWOS_COMPLEMENT;
            default -> throw new IllegalArgumentException("Unknown encoding: " + value);
        };
    }
}
