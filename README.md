# NimbusLink

NimbusLink is a lightweight messaging protocol system that combines a custom TCP protocol (NBP) with a Spring Boot backend and a real-time JSP admin dashboard.

## Architecture

- **TCP Server**: Handles raw TCP connections using the NBP protocol.
- **Spring Boot**: Hosts the TCP server, Web application, and WebSocket bridge.
- **WebSocket Bridge**: Forwards TCP events to the web dashboard in real-time.
- **JSP Dashboard**: Admin interface to monitor and control the server.

## NBP Protocol

The NimbusLink Protocol (NBP) is a simple text-based line protocol.

### Commands

- `PING`: Heartbeat check. Server responds with `ACK PONG`.
- `NICK <nickname>`: Set your display name. Server responds with `ACK Nickname set to: <nickname>`.
- `JOIN <room>`: Join a specific room/channel.
- `LEAVE <room>`: Leave a room.
- `SEND <room> <message>`: Send a message to a room.
- `ACK <message>`: Server acknowledgment.
- `ERR <message>`: Error message.

## Setup & Run

### Prerequisites
- Java 8
- Gradle (wrapper included)

### Running the Server

```bash
./gradlew bootRun
```

The server will start on:
- **Web Dashboard**: http://localhost:8080
- **TCP Server**: localhost:8888

### Running the CLI Client

You can run the included simple Java client:

```bash
# Compile and run (simplified)
./gradlew classes
java -cp build/classes/java/main com.nimbuslink.client.ConsoleClient
```

Or use `netcat`:
```bash
nc localhost 8888
```

## Admin Dashboard

Access http://localhost:8080 to:
- View connected client count.
- See active rooms.
- Monitor live logs.
- Broadcast messages to all clients.
- Kick clients by ID.
