package exception;

public class InvalidMaskingParameterException extends IllegalArgumentException {
    public InvalidMaskingParameterException(String param) {
        super("Invalid masking parameter: " + param);
    }
}