package uk.ac.westminster.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.Map;

// Root of the API at GET /api/v1 - returns metadata and a map of links
// (HATEOAS). Part 1.2 of the coursework.
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover(@Context UriInfo uriInfo) {
        // absolute base URI so the links work no matter where the app is deployed
        String base = uriInfo.getBaseUri().toString();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        // LinkedHashMap to keep a stable field order in the JSON output
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("apiName", "Smart Campus Sensor & Room Management API");
        body.put("version", "1.0.0");
        body.put("description", "RESTful API for managing campus rooms, sensors and readings.");
        body.put("documentation", "https://github.com/<your-username>/smart-campus-api#readme");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team", "Smart Campus Backend");
        contact.put("email", "smart-campus@westminster.ac.uk");
        body.put("contact", contact);

        // links to the main resource collections (HATEOAS)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", base + "/api/v1");
        links.put("rooms", base + "/api/v1/rooms");
        links.put("sensors", base + "/api/v1/sensors");
        links.put("sensors-by-type", base + "/api/v1/sensors?type={type}");
        links.put("sensor-readings", base + "/api/v1/sensors/{sensorId}/readings");
        body.put("_links", links);

        return Response.ok(body).build();
    }

    // Endpoint used ONLY for the video demo of Part 5.4 (global 500 safety net).
    // Forces a NullPointerException so I can show that the catch-all
    // ExceptionMapper returns a clean JSON error instead of a stack trace.
    @GET
    @Path("/trigger-error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError() {
        String s = null;
        return Response.ok(s.length()).build();
    }
}
