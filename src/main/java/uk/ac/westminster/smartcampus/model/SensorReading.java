package uk.ac.westminster.smartcampus.model;

import java.util.UUID;

// One reading recorded by a sensor.
public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {
    }

    // handy when I only have the value (used in DataStore.seed)
    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
