package uk.ac.westminster.smartcampus.mapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import uk.ac.westminster.smartcampus.model.ErrorResponse;

// By default Jersey returns an HTML 404 page. I want JSON everywhere, so
// I catch NotFoundException and return the same ErrorResponse shape.
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = "The requested resource does not exist.";
        }

        ErrorResponse body = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                "RESOURCE_NOT_FOUND",
                msg);

        return Response.status(Response.Status.NOT_FOUND)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
