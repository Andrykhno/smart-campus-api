package uk.ac.westminster.smartcampus.app;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;
import java.util.logging.Logger;

// Starts the embedded Grizzly server and deploys our JAX-RS app.
// Run with: mvn exec:java
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), new SmartCampusApplication());

        // make sure the port is released on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Stopping Smart Campus API...");
            server.shutdownNow();
        }));

        LOGGER.info("Smart Campus API is running at " + BASE_URI + "api/v1");
        LOGGER.info("Press Ctrl+C to stop.");
    }
}
