# Smart Campus Sensor & Room Management API

Coursework for **5COSC022W Client-Server Architectures (2025/26)**.

A JAX-RS REST service for managing campus rooms, sensors, and sensor readings.
Built with Jersey on an embedded Grizzly server. No Spring, no database —
just in-memory `ConcurrentHashMap`/`ArrayList` as required by the brief.

## 1. How to build and run

Requirements: **JDK 17** and **Maven 3.9+**.

Run from source (easiest):

```
mvn clean compile exec:java
```

Or build a runnable JAR and run that:

```
mvn clean package
java -jar target/smart-campus-api.jar
```

The server listens on `http://localhost:8080/`. The API base path is
`/api/v1`. To check it's running:

```
curl http://localhost:8080/api/v1
```

Stop with Ctrl+C.

## 2. Project layout

```
src/main/java/uk/ac/westminster/smartcampus/
  app/       - Main + SmartCampusApplication (JAX-RS entry point)
  model/     - Room, Sensor, SensorReading, ErrorResponse POJOs
  store/     - DataStore singleton (ConcurrentHashMap)
  resource/  - DiscoveryResource, SensorRoomResource, SensorResource,
               SensorReadingResource
  exception/ - custom runtime exceptions
  mapper/    - ExceptionMappers that turn exceptions into JSON errors
  filter/    - LoggingFilter (logs every request and response)
```

## 3. API overview

Base: `http://localhost:8080/api/v1`

| Method | Path                                   | Notes                                |
|--------|----------------------------------------|--------------------------------------|
| GET    | `/`                                    | Discovery (HATEOAS links)            |
| GET    | `/rooms`                               | List all rooms                       |
| GET    | `/rooms/{roomId}`                      | Single room (404 if missing)         |
| POST   | `/rooms`                               | Create room (201 + Location)         |
| DELETE | `/rooms/{roomId}`                      | 204 ok, 404 missing, 409 not empty   |
| GET    | `/sensors`                             | List all sensors                     |
| GET    | `/sensors?type=Temperature`            | Filter by type (case-insensitive)    |
| GET    | `/sensors/{sensorId}`                  | Single sensor (404 if missing)       |
| POST   | `/sensors`                             | Create sensor; 422 if roomId missing |
| GET    | `/sensors/{sensorId}/readings`         | Reading history                      |
| POST   | `/sensors/{sensorId}/readings`         | Add reading (403 if MAINTENANCE)     |
| GET    | `/trigger-error`                       | Demo of global 500 handler           |

On startup the app pre-loads two rooms (LIB-301, LEC-112) and three sensors
(TEMP-001, CO2-001, OCC-001), so the demo works without any POSTs first.

All error responses use the same JSON shape:

```json
{
  "status": 409,
  "error": "ROOM_NOT_EMPTY",
  "message": "Room 'LIB-301' still contains 2 active sensor(s) and cannot be deleted.",
  "timestamp": 1745234567890
}
```

## 4. Sample curl commands

```
# 1. Discovery
curl http://localhost:8080/api/v1

# 2. Create a room
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"LAB-007","name":"Robotics Lab","capacity":15}'

# 3. Create a sensor in that room
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"type":"Humidity","roomId":"LAB-007"}'

# 4. Try to create a sensor in a non-existent room (-> 422)
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"type":"Temperature","roomId":"GHOST-999"}'

# 5. Filter sensors by type
curl "http://localhost:8080/api/v1/sensors?type=Temperature"

# 6. Post a reading, then re-check the sensor (currentValue should update)
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":22.4}'
curl http://localhost:8080/api/v1/sensors/TEMP-001

# 7. Post a reading to a sensor in MAINTENANCE (-> 403)
curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":7}'

# 8. Delete a room that still has sensors (-> 409)
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

## 5. Answers to the report questions

### Q1.1 — JAX-RS resource lifecycle

By default Jersey creates a new instance of each resource class for every
incoming request. So if two clients both call `GET /rooms` at the same
time, each one gets its own `SensorRoomResource` object. You can also
register resources as singletons, but that's not the default.

The practical impact is that any mutable state on the resource instance
is safe by default — two threads can't step on each other because they
literally don't share the object. But any shared data (like our rooms
and sensors) needs to live somewhere else.

That's why I put everything in `DataStore`, which is a singleton. Since
multiple threads can hit it at the same time, I use `ConcurrentHashMap`
for all three maps (`rooms`, `sensors`, `readings`). This avoids race
conditions on writes without me having to write `synchronized` blocks
everywhere.

### Q1.2 — HATEOAS benefits

The discovery endpoint at `GET /api/v1` returns a JSON body with
metadata (name, version, contact) and a `_links` map pointing at the
main resource collections. Clients can read this one response and know
exactly which URIs to call next, instead of memorising them or reading
a separate document.

Compared to static documentation this is much better because:

- the client only needs to know the base URL;
- if we move or rename a resource, we just update the links and existing
  clients don't break (as long as they look up URIs dynamically);
- the response itself is machine-readable, so tools like Postman can
  generate requests straight from it;
- we can put additional runtime info in the same place (version,
  support email), which also makes this endpoint useful as a health
  check.

### Q2.1 — Full objects vs IDs in the list response

I chose to return the full `Room` objects, not just the IDs. The
trade-off is basically bandwidth vs the number of calls.

If we only returned IDs, the list response would be tiny, but the
client would then have to call `GET /rooms/{id}` once per room to
actually show anything. For 100 rooms that's 100 extra requests and
100 extra round-trips.

Returning the full objects makes the response a bit bigger but
eliminates all those follow-up calls. Since `Room` has only a few
fields (id, name, capacity, sensorIds), the payload stays small. For
this coursework the full-object approach is the right call. If the
model were much bigger or the collection had thousands of rooms, I
might add pagination or a `?fields=...` parameter.

### Q2.2 — Is DELETE idempotent?

Yes, the server state ends up the same no matter how many times you
send the same `DELETE /rooms/{roomId}`. After the first call the room
is gone, and repeating the call can't "re-delete" something that isn't
there.

What changes is the response. The first call returns **204 No Content**
(success). Any repeat call returns **404 Not Found** because the room
is missing now. Idempotency is about the server state, not about the
response being identical — and in both cases the state is the same:
the room doesn't exist.

There's also a business rule: if the room still has sensors, we return
**409 Conflict** with `ROOM_NOT_EMPTY`. Repeating that failed call
returns the same 409. So all three outcomes (204, 404, 409) are
idempotent.

### Q3.1 — What happens if the client sends the wrong Content-Type

All POST endpoints have `@Consumes(MediaType.APPLICATION_JSON)`. This
tells Jersey two things:

1. When a matching request comes in, use the Jackson `MessageBodyReader`
   to turn the JSON body into the Java object (`Room`, `Sensor`, etc).
2. If the client sends something else (for example `text/plain` or
   `application/xml`), don't call the method at all — Jersey returns
   **HTTP 415 Unsupported Media Type** automatically.

That 415 then goes through my `GlobalExceptionMapper` and comes out as a
clean JSON error. So the contract is strict: JSON in, JSON out. The
client gets a clear, standards-compliant refusal instead of a weird
500 or a malformed object.

### Q3.2 — Why `@QueryParam` and not a path segment for the filter

The `type` filter is done with `@QueryParam("type")`, so the URL looks
like `/sensors?type=Temperature` instead of `/sensors/type/Temperature`.
Query parameters are the natural choice because:

- they are optional — `@QueryParam` just gives you `null` when missing,
  so one method handles both the filtered and unfiltered case;
- they compose — `?type=Temperature&status=ACTIVE` adds a second
  filter without changing the URL structure;
- they don't pretend to be a new resource. A path segment implies a
  new thing being addressed. But `/sensors/type/CO2` isn't really a
  different resource from `/sensors` — it's just a view of the same
  collection.

Treating the filter as a query parameter keeps the URI design clean.

### Q4.1 — Sub-resource locator benefits

In `SensorResource` I have this method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource readings(@PathParam("sensorId") String sensorId) {
    if (!store.sensorExists(sensorId)) {
        throw new NotFoundException("Sensor '" + sensorId + "' not found.");
    }
    return new SensorReadingResource(sensorId);
}
```

There's no `@GET` or `@POST` on it — that's what makes it a *locator*.
Jersey calls this method, gets a `SensorReadingResource` back, and
then continues routing on that instance.

The benefits:

- **Separation.** `SensorResource` only deals with sensor-level CRUD.
  Readings logic lives in its own class. Neither class gets bloated.
- **URL mirrors code.** `/sensors/{id}/readings` → `SensorResource`
  → `SensorReadingResource`. Easy to navigate.
- **One validation point.** The locator checks the sensor exists
  before returning the child. Every method on `SensorReadingResource`
  can then assume the sensor is there, without repeating the check.
- **Scales better.** If I had to put every reading method on
  `SensorResource`, that class would grow and grow. With the locator
  pattern each nested resource becomes its own class.

### Q5.2 — Why 422, not 404, for a missing linked resource

If a client posts a new sensor with `"roomId": "GHOST-999"`, I return
**422 Unprocessable Entity**, not 404.

404 means "the resource you addressed doesn't exist". But the URL we
hit is `/api/v1/sensors`, and that endpoint is there — you can POST
to it. The problem isn't the URL; it's the payload. The JSON is well
formed, the target endpoint exists, but a value inside it points at
something that isn't in the system.

That's exactly what 422 is for. The error code `LINKED_RESOURCE_NOT_FOUND`
tells the client which field caused the problem so they can fix it.
Using 404 here would confuse clients and monitoring tools into
thinking the `/sensors` endpoint itself is gone.

### Q5.4 — Security risks of exposing stack traces

My `GlobalExceptionMapper` catches every unhandled `Throwable`. It
**logs the full trace on the server** and sends the client a short
generic message (`"An unexpected error occurred. Please try again
later."`) with status 500.

Stack traces are very useful for developers but dangerous to expose
publicly. A trace can tell an attacker:

- which framework and version you're running (they can then search
  known vulnerabilities for those exact versions);
- your package structure and internal class names;
- file paths from the server's filesystem;
- sometimes SQL queries or fragments of business logic.

Any of that makes it easier to plan a targeted attack. The default
Grizzly/Jersey error page includes the stack trace, which is why a
catch-all mapper is important: we replace that with a clean JSON
response that gives nothing away. The `/trigger-error` endpoint in
`DiscoveryResource` exists just to demonstrate this during the video:
it forces a NullPointerException, and the client still gets a calm
JSON body while the trace appears only in my local terminal.

### Q5.5 — Why filters instead of inline logging

I use `LoggingFilter`, which implements both `ContainerRequestFilter`
and `ContainerResponseFilter`. It runs on the way in and on the way
out for every endpoint — no resource method has to know about it.

If I put `LOGGER.info(...)` calls inside each resource method
instead, I would:

- have to remember to add them to every new endpoint I write;
- end up with slightly different formats in different methods;
- not see the final HTTP status if the method threw an exception
  (the filter sees the mapped status, but an inline log line inside
  the method can't).

A single filter gives me consistent, automatic logs with the final
status code, and it's trivial to turn off by just removing the
`@Provider` annotation or commenting out one package scan.
