package uk.ac.westminster.smartcampus.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundException;
import uk.ac.westminster.smartcampus.model.ErrorResponse;

// Turns LinkedResourceNotFoundException into HTTP 422.
// Note: 422 isn't in the Response.Status enum so I just use the number.
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                UNPROCESSABLE_ENTITY,
                "LINKED_RESOURCE_NOT_FOUND",
                ex.getMessage());

        return Response.status(UNPROCESSABLE_ENTITY)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
