package uk.ac.westminster.smartcampus.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import uk.ac.westminster.smartcampus.exception.RoomNotEmptyException;
import uk.ac.westminster.smartcampus.model.ErrorResponse;

// Turns RoomNotEmptyException into an HTTP 409 JSON response.
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorResponse body = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                "ROOM_NOT_EMPTY",
                ex.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
