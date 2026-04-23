package uk.ac.westminster.smartcampus.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uk.ac.westminster.smartcampus.exception.SensorUnavailableException;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;
import uk.ac.westminster.smartcampus.store.DataStore;

import java.util.List;
import java.util.UUID;

// Sub-resource that handles /sensors/{sensorId}/readings.
// Built by SensorResource.readings(...), NOT a root JAX-RS resource
// (that's why there's no @Path on this class).
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings  -> reading history
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> history() {
        return store.findReadings(sensorId);
    }

    // POST /api/v1/sensors/{sensorId}/readings -> append new reading.
    // Side effect (Part 4.2): updates currentValue on the sensor.
    // If the sensor is in MAINTENANCE/OFFLINE we return 403.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.findSensor(sensorId);
        if (sensor == null) {
            // shouldn't really happen because the locator checks it,
            // but just in case
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!Sensor.STATUS_ACTIVE.equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // fill in id/timestamp if the client only sent {"value": ...}
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading); // also updates currentValue
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
