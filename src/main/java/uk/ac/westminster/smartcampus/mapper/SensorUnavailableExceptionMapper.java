package uk.ac.westminster.smartcampus.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import uk.ac.westminster.smartcampus.exception.SensorUnavailableException;
import uk.ac.westminster.smartcampus.model.ErrorResponse;

// SensorUnavailableException -> HTTP 403.
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorResponse body = new ErrorResponse(
                Response.Status.FORBIDDEN.getStatusCode(),
                "SENSOR_UNAVAILABLE",
                ex.getMessage());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
