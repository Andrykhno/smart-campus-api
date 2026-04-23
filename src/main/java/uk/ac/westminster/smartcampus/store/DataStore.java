package uk.ac.westminster.smartcampus.store;

import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// In-memory data store. We can't use a DB (coursework rule), so everything
// lives in ConcurrentHashMap/ArrayList. Singleton because JAX-RS creates
// a new resource instance per request and I want one shared state.
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    // readings per sensor id
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ---------- Rooms ----------

    public List<Room> findAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room findRoom(String id) {
        return rooms.get(id);
    }

    public void saveRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ---------- Sensors ----------

    public List<Sensor> findAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public Sensor findSensor(String id) {
        return sensors.get(id);
    }

    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);

        // keep room -> sensor link in sync so DELETE room can check if empty
        Room parent = rooms.get(sensor.getRoomId());
        if (parent != null && !parent.getSensorIds().contains(sensor.getId())) {
            parent.getSensorIds().add(sensor.getId());
        }

        // make sure the sensor has an empty readings list ready
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // ---------- Readings ----------

    public List<SensorReading> findReadings(String sensorId) {
        List<SensorReading> list = readings.get(sensorId);
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }

    // Adds a reading AND updates currentValue on the sensor (Part 4.2 side effect).
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }

    // ---------- Demo data ----------

    // Pre-populates a couple of rooms/sensors so the API isn't empty on first run.
    // Makes the Postman demo easier.
    private void seed() {
        Room library = new Room("LIB-301", "Library Quiet Study", 40);
        Room lecture = new Room("LEC-112", "Lecture Theatre C", 120);
        saveRoom(library);
        saveRoom(lecture);

        Sensor temp = new Sensor("TEMP-001", "Temperature", Sensor.STATUS_ACTIVE, 21.5, "LIB-301");
        Sensor co2 = new Sensor("CO2-001", "CO2", Sensor.STATUS_ACTIVE, 450.0, "LIB-301");
        // one in MAINTENANCE so I can demo the 403 response
        Sensor occ = new Sensor("OCC-001", "Occupancy", Sensor.STATUS_MAINTENANCE, 0.0, "LEC-112");
        saveSensor(temp);
        saveSensor(co2);
        saveSensor(occ);

        addReading("TEMP-001", new SensorReading(21.2));
        addReading("TEMP-001", new SensorReading(21.5));
        addReading("CO2-001", new SensorReading(440.0));
    }
}
