package uk.ac.westminster.smartcampus.exception;

// Thrown when trying to delete a room that still has sensors in it.
// Mapped to HTTP 409 by the exception mapper.
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room '" + roomId + "' still contains " + sensorCount
                + " active sensor(s) and cannot be deleted.");
        this.roomId = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getSensorCount() {
        return sensorCount;
    }
}
