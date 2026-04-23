package uk.ac.westminster.smartcampus.exception;

// Thrown when the request body references something that doesn't exist
// (e.g. posting a sensor with a roomId that isn't in the store).
// Mapped to HTTP 422.
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String referencedField;
    private final String referencedValue;

    public LinkedResourceNotFoundException(String referencedField, String referencedValue) {
        super("Referenced resource not found: " + referencedField + "='" + referencedValue + "'.");
        this.referencedField = referencedField;
        this.referencedValue = referencedValue;
    }

    public String getReferencedField() {
        return referencedField;
    }

    public String getReferencedValue() {
        return referencedValue;
    }
}
