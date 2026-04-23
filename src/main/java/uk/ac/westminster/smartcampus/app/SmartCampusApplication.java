package uk.ac.westminster.smartcampus.app;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

// Main JAX-RS application. Base path is /api/v1 as required by the brief.
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // scan packages so I don't have to register every class manually
        packages("uk.ac.westminster.smartcampus.resource",
                 "uk.ac.westminster.smartcampus.mapper",
                 "uk.ac.westminster.smartcampus.filter");

        // use Jackson for JSON
        register(JacksonFeature.class);
    }
}
