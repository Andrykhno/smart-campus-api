package uk.ac.westminster.smartcampus.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import uk.ac.westminster.smartcampus.exception.RoomNotEmptyException;
import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.store.DataStore;

import java.net.URI;
import java.util.List;
import java.util.UUID;

// Room management - Part 2 of the coursework.
@Path("/rooms")
public class SensorRoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms  -> list every room.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> listRooms() {
        return store.findAllRooms();
    }

    // GET /api/v1/rooms/{roomId}  -> one room.
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = store.findRoom(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' not found.");
        }
        return room;
    }

    // POST /api/v1/rooms  -> create a new room, returns 201 + Location header.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        // auto-generate an id if the client didn't send one
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId("ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        store.saveRoom(room);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();
        return Response.created(location).entity(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    // 404 if room doesn't exist, 409 if it still has sensors, 204 otherwise.
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.findRoom(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' not found.");
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
