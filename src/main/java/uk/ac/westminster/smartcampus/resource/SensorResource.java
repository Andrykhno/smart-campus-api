package uk.ac.westminster.smartcampus.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.store.DataStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Sensor management - Part 3 of the coursework.
// Also contains the sub-resource locator for Part 4.
@Path("/sensors")
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors               -> every sensor
    // GET /api/v1/sensors?type=XYZ      -> only sensors of that type (case-insensitive)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> listSensors(@QueryParam("type") String type) {
        List<Sensor> all = store.findAllSensors();
        if (type == null || type.isBlank()) {
            return all;
        }

        // simple filter, case-insensitive so "co2" and "CO2" both work
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : all) {
            if (s.getType() != null && s.getType().equalsIgnoreCase(type)) {
                result.add(s);
            }
        }
        return result;
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.findSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return sensor;
    }

    // POST /api/v1/sensors  -> register a new sensor.
    // Checks that the roomId actually exists (Part 3.1 / 5.2).
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getRoomId() == null || !store.roomExists(sensor.getRoomId())) {
            String badValue = sensor.getRoomId();
            if (badValue == null) {
                badValue = "null";
            }
            throw new LinkedResourceNotFoundException("roomId", badValue);
        }

        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId("SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus(Sensor.STATUS_ACTIVE);
        }
        store.saveSensor(sensor);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();
        return Response.created(location).entity(sensor).build();
    }

    // Sub-resource locator for Part 4.
    // Note: no @GET/@POST annotation - that's what makes it a locator.
    // Jersey calls this method, gets a SensorReadingResource back,
    // and then continues routing on it.
    @Path("/{sensorId}/readings")
    public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
        if (!store.sensorExists(sensorId)) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
