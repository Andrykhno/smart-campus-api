package uk.ac.westminster.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

// Room POJO (see coursework spec page 3).
public class Room {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds = new ArrayList<>();

    public Room() {
        // needed for Jackson
    }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) {
        // avoid null so isEmpty() checks are safe
        this.sensorIds = sensorIds != null ? sensorIds : new ArrayList<>();
    }
}
