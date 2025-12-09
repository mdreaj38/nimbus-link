package com.nimbuslink.server;

import com.nimbuslink.protocol.NbpCommand;
import com.nimbuslink.protocol.NbpMessage;
import com.nimbuslink.protocol.NbpParser;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final SubscriptionManager subscriptionManager;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;

    @Getter
    private final String clientId;

    private String nickname;

    public ClientHandler(Socket socket, SubscriptionManager subscriptionManager) {
        this.socket = socket;
        this.subscriptionManager = subscriptionManager;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    public String getDisplayName() {
        return nickname != null ? nickname : clientId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            subscriptionManager.addClient(this);
            logger.info("Client connected: " + clientId);

            String line;
            while (running && (line = in.readLine()) != null) {
                handleLine(line);
            }
        } catch (SocketException e) {
            logger.info("Client disconnected (SocketException): " + clientId);
        } catch (IOException e) {
            logger.error("Error handling client " + clientId, e);
        } finally {
            close();
        }
    }

    private void handleLine(String line) {
        NbpMessage message = NbpParser.parse(line);
        if (message == null)
            return;

        logger.debug("Received from {}: {}", clientId, message);

        switch (message.getCommand()) {
            case PING:
                sendMessage(NbpMessage.ack("PONG"));
                break;
            case NICK:
                String newNickname = message.getPayload().trim();
                if (newNickname.isEmpty()) {
                    sendMessage(NbpMessage.err("Nickname cannot be empty"));
                } else {
                    this.nickname = newNickname;
                    subscriptionManager.onNicknameSet(this, newNickname);
                    sendMessage(NbpMessage.ack("Nickname set to: " + newNickname));
                }
                break;
            case JOIN:
                subscriptionManager.joinRoom(message.getPayload(), this);
                sendMessage(NbpMessage.ack("Joined " + message.getPayload()));
                break;
            case LEAVE:
                subscriptionManager.leaveRoom(message.getPayload(), this);
                sendMessage(NbpMessage.ack("Left " + message.getPayload()));
                break;
            case SEND:
                // Format: ROOM MESSAGE
                String[] parts = message.getPayload().split("\\s+", 2);
                if (parts.length == 2) {
                    String room = parts[0];
                    String msgContent = parts[1];
                    // Broadcast to room
                    subscriptionManager.broadcastToRoom(room,
                            new NbpMessage(NbpCommand.SEND, room + " " + getDisplayName() + ": " + msgContent), this);
                } else {
                    sendMessage(NbpMessage.err("Invalid SEND format. Usage: SEND <ROOM> <MESSAGE>"));
                }
                break;
            default:
                sendMessage(NbpMessage.err("Unknown or unsupported command"));
        }
    }

    public void sendMessage(NbpMessage message) {
        if (out != null) {
            out.print(NbpParser.serialize(message));
            out.flush();
        }
    }

    public void close() {
        running = false;
        subscriptionManager.removeClient(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        logger.info("Client handler closed: " + clientId);
    }

    public void kick() {
        sendMessage(new NbpMessage(NbpCommand.ERR, "You have been kicked by admin."));
        close();
    }
}
