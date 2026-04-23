package uk.ac.westminster.smartcampus.exception;

// Thrown when someone tries to POST a reading to a sensor that is in
// MAINTENANCE or OFFLINE. Mapped to HTTP 403.
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorUnavailableException(String sensorId, String currentStatus) {
        super("Sensor '" + sensorId + "' is currently '" + currentStatus
                + "' and cannot accept new readings.");
        this.sensorId = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
