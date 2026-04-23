package uk.ac.westminster.smartcampus.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import uk.ac.westminster.smartcampus.model.ErrorResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

// Catch-all for Part 5.4. Any exception not handled by a more specific
// mapper lands here. We never leak stack traces to the client.
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable throwable) {
        // If this is a Jersey HTTP exception (e.g. 415 when the client
        // sends a wrong Content-Type), just forward the status code
        // but still return our JSON error shape.
        if (throwable instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) throwable;
            int status = wae.getResponse().getStatus();

            String msg = throwable.getMessage();
            if (msg == null) {
                msg = "Request could not be processed.";
            }

            ErrorResponse body = new ErrorResponse(status, statusName(status), msg);
            return Response.status(status)
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Real server-side bug. Log the full trace for us, but send a
        // generic message to the client.
        LOGGER.log(Level.SEVERE, "Unhandled exception in resource method", throwable);

        ErrorResponse body = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String statusName(int code) {
        Response.Status s = Response.Status.fromStatusCode(code);
        if (s != null) {
            return s.name();
        }
        return "HTTP_" + code;
    }
}
